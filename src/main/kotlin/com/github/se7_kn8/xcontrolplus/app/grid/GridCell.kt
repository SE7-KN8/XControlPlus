package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.util.rotated
import com.github.se7_kn8.xcontrolplus.gridview.CellRotation
import com.github.se7_kn8.xcontrolplus.gridview.model.GridCell
import com.github.se7_kn8.xcontrolplus.gridview.GridRenderer
import javafx.scene.canvas.GraphicsContext

enum class GridCellRenderer {
    STRAIGHT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            gc.fillRect(
                renderer.getPosX(gridX.toDouble()),
                (gridY.toDouble() + 0.3) * renderer.gridSize, renderer.gridSize, 0.4 * renderer.gridSize
            )
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            gc.fillRect(
                renderer.getPosX(gridX.toDouble()),
                (gridY.toDouble() + 0.4) * renderer.gridSize, renderer.gridSize, 0.2 * renderer.gridSize
            )
        }
    },

    TURN {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            val x =
                doubleArrayOf(
                    (gridX.toDouble() + 0.3) * renderer.gridSize,
                    (gridX.toDouble() + 0.7) * renderer.gridSize,
                    (gridX.toDouble() + 0.0) * renderer.gridSize,
                    (gridX.toDouble() + 0.0) * renderer.gridSize
                )
            val y =
                doubleArrayOf(
                    (gridY.toDouble() + 1.0) * renderer.gridSize,
                    (gridY.toDouble() + 1.0) * renderer.gridSize,
                    (gridY.toDouble() + 0.3) * renderer.gridSize,
                    (gridY.toDouble() + 0.7) * renderer.gridSize
                )
            gc.fillPolygon(x, y, 4)
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            val x =
                doubleArrayOf(
                    (gridX.toDouble() + 0.4) * renderer.gridSize,
                    (gridX.toDouble() + 0.6) * renderer.gridSize,
                    (gridX.toDouble() + 0.0) * renderer.gridSize,
                    (gridX.toDouble() + 0.0) * renderer.gridSize
                )
            val y =
                doubleArrayOf(
                    (gridY.toDouble() + 1.0) * renderer.gridSize,
                    (gridY.toDouble() + 1.0) * renderer.gridSize,
                    (gridY.toDouble() + 0.4) * renderer.gridSize,
                    (gridY.toDouble() + 0.6) * renderer.gridSize
                )
            gc.fillPolygon(x, y, 4)
        }

    },

    LEFT_TURNOUT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            STRAIGHT.drawBackground(gridX, gridY, gc, renderer, cell)
            gc.rotated(
                90.0, renderer.getMidX(gridX.toDouble()),
                renderer.getMidY(gridY.toDouble())
            ) {
                TURN.drawBackground(gridX, gridY, gc, renderer, cell)
            }
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            if (cell is TurnoutGridCell) {
                if (cell.turned) {
                    gc.rotated(90.0, renderer.getMidX(gridX.toDouble()), renderer.getMidY(gridY.toDouble())) {
                        TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                    }
                } else {
                    STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                }
            } else {
                STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                gc.rotated(90.0, renderer.getMidX(gridX.toDouble()), renderer.getMidY(gridY.toDouble())) {
                    TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                }
            }
        }

    },

    RIGHT_TURNOUT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            STRAIGHT.drawBackground(gridX, gridY, gc, renderer, cell)
            TURN.drawBackground(gridX, gridY, gc, renderer, cell)
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            if (cell is TurnoutGridCell) {
                if (cell.turned) {
                    TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                } else {
                    STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                }
            } else {
                STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                TURN.drawForeground(gridX, gridY, gc, renderer, cell)
            }
        }

    },
    ;


    abstract fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell? = null)
    abstract fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell? = null)

    fun draw(x: Int, y: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell? = null) {
        gc.fill = Colors.track
        drawBackground(x, y, gc, renderer, cell)
        gc.fill = Colors.trackHighlight
        drawForeground(x, y, gc, renderer, cell)
    }

}

abstract class BaseCell(gridX: Int, gridY: Int, rot: CellRotation) : GridCell(gridX, gridY, rot) {
    override fun render(now: Long, gc: GraphicsContext, renderer: GridRenderer<out GridCell>) {
        getRenderer().draw(gridX, gridY, gc, renderer, this)
    }

    abstract fun getRenderer(): GridCellRenderer
}

class StraightGridCell(x: Int, y: Int, rot: CellRotation) : BaseCell(x, y, rot) {
    override fun getRenderer() = GridCellRenderer.STRAIGHT
}

class TurnGridCell(x: Int, y: Int, rot: CellRotation) : BaseCell(x, y, rot) {
    override fun getRenderer() = GridCellRenderer.TURN
}

enum class TurnoutType {
    LEFT {
        override fun getRenderer() = GridCellRenderer.LEFT_TURNOUT

    },
    RIGHT {
        override fun getRenderer() = GridCellRenderer.RIGHT_TURNOUT
    };

    abstract fun getRenderer(): GridCellRenderer
}

class TurnoutGridCell(x: Int, y: Int, rot: CellRotation, val turnoutType: TurnoutType) : BaseCell(x, y, rot) {
    var turned = false
    override fun getRenderer() = turnoutType.getRenderer()
}

