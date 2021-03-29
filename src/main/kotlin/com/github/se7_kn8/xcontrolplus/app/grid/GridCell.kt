package com.github.se7_kn8.xcontrolplus.app.grid

import javafx.scene.canvas.GraphicsContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class GridCellRenderer {
    STRAIGHT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell?) {
            gc.fillRect(
                gridX.toDouble() * GRID_SIZE,
                (gridY.toDouble() + 0.3) * GRID_SIZE, GRID_SIZE.toDouble(), 0.4 * GRID_SIZE.toDouble()
            )
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell?) {
            gc.fillRect(
                gridX.toDouble() * GRID_SIZE,
                (gridY.toDouble() + 0.4) * GRID_SIZE, GRID_SIZE.toDouble(), 0.2 * GRID_SIZE.toDouble()
            )
        }
    },

    TURN {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell?) {
            val x =
                doubleArrayOf(
                    (gridX.toDouble() + 0.3) * GRID_SIZE,
                    (gridX.toDouble() + 0.7) * GRID_SIZE,
                    (gridX.toDouble() + 0.0) * GRID_SIZE,
                    (gridX.toDouble() + 0.0) * GRID_SIZE
                )
            val y =
                doubleArrayOf(
                    (gridY.toDouble() + 1.0) * GRID_SIZE,
                    (gridY.toDouble() + 1.0) * GRID_SIZE,
                    (gridY.toDouble() + 0.3) * GRID_SIZE,
                    (gridY.toDouble() + 0.7) * GRID_SIZE
                )
            gc.fillPolygon(x, y, 4)
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell?) {
            val x =
                doubleArrayOf(
                    (gridX.toDouble() + 0.4) * GRID_SIZE,
                    (gridX.toDouble() + 0.6) * GRID_SIZE,
                    (gridX.toDouble() + 0.0) * GRID_SIZE,
                    (gridX.toDouble() + 0.0) * GRID_SIZE
                )
            val y =
                doubleArrayOf(
                    (gridY.toDouble() + 1.0) * GRID_SIZE,
                    (gridY.toDouble() + 1.0) * GRID_SIZE,
                    (gridY.toDouble() + 0.4) * GRID_SIZE,
                    (gridY.toDouble() + 0.6) * GRID_SIZE
                )
            gc.fillPolygon(x, y, 4)
        }

    },

    LEFT_TURNOUT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell?) {
            STRAIGHT.drawBackground(gridX, gridY, gc, cell)
            gc.rotated(90.0, (gridX * GRID_SIZE + GRID_SIZE / 2).toDouble(), (gridY * GRID_SIZE + GRID_SIZE / 2).toDouble()) {
                TURN.drawBackground(gridX, gridY, gc, cell)
            }
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell?) {
            if (cell is TurnoutGridCell) {
                if (cell.turned) {
                    gc.rotated(90.0, (gridX * GRID_SIZE + GRID_SIZE / 2).toDouble(), (gridY * GRID_SIZE + GRID_SIZE / 2).toDouble()) {
                        TURN.drawForeground(gridX, gridY, gc, cell)
                    }
                } else {
                    STRAIGHT.drawForeground(gridX, gridY, gc, cell)
                }
            } else {
                STRAIGHT.drawForeground(gridX, gridY, gc, cell)
                gc.rotated(90.0, (gridX * GRID_SIZE + GRID_SIZE / 2).toDouble(), (gridY * GRID_SIZE + GRID_SIZE / 2).toDouble()) {
                    TURN.drawForeground(gridX, gridY, gc, cell)
                }
            }
        }

    },

    RIGHT_TURNOUT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell?) {
            STRAIGHT.drawBackground(gridX, gridY, gc, cell)
            TURN.drawBackground(gridX, gridY, gc, cell)
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell?) {
            if (cell is TurnoutGridCell) {
                if (cell.turned) {
                    TURN.drawForeground(gridX, gridY, gc, cell)
                } else {
                    STRAIGHT.drawForeground(gridX, gridY, gc, cell)
                }
            } else {
                STRAIGHT.drawForeground(gridX, gridY, gc, cell)
                TURN.drawForeground(gridX, gridY, gc, cell)
            }
        }

    },
    ;


    abstract fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell? = null)
    abstract fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell? = null)

    fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, cell: GridCell? = null) {
        gc.fill = Colors.track
        drawBackground(gridX, gridY, gc, cell)
        gc.fill = Colors.trackHighlight
        drawForeground(gridX, gridY, gc, cell)
    }

}

@Serializable
sealed class GridCell {

    abstract val gridX: Int
    abstract val gridY: Int
    abstract val rot: Rotation


    fun getPosX() = gridX * GRID_SIZE
    fun getMidPosX() = getPosX() + GRID_SIZE / 2

    fun getPosY() = gridY * GRID_SIZE
    fun getMidPosY() = getPosY() + GRID_SIZE / 2

    abstract fun getRenderer(): GridCellRenderer
}

@Serializable
@SerialName("straight")
class StraightGridCell(override val gridX: Int, override val gridY: Int, override val rot: Rotation) : GridCell() {
    override fun getRenderer() = GridCellRenderer.STRAIGHT
}

@Serializable
@SerialName("turn")
class TurnGridCell(override val gridX: Int, override val gridY: Int, override val rot: Rotation) : GridCell() {
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

@Serializable
@SerialName("turnout")
class TurnoutGridCell(override val gridX: Int, override val gridY: Int, override val rot: Rotation, val turnoutType: TurnoutType) : GridCell() {
    var turned = false
    override fun getRenderer() = turnoutType.getRenderer()
}

