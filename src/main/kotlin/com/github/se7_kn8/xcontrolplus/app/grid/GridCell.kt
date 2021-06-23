package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.util.rotated
import com.github.se7_kn8.xcontrolplus.gridview.GridRenderer
import com.github.se7_kn8.xcontrolplus.gridview.model.GridCell
import javafx.beans.property.Property
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.MenuItem
import javafx.scene.text.Font
import java.util.function.Consumer

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

    TEXT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            // NOP
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            gc.fill = Colors.text
            if (cell is TextGridCell) {
                gc.font = Font.font(cell.fontSize.doubleValue())
                gc.fillText(cell.text.get(), (gridX + 0.1) * renderer.gridSize, (gridY + 0.5) * renderer.gridSize)
            } else {
                gc.font = Font.font(10.0)
                gc.fillText("Text", (gridX + 0.1) * renderer.gridSize, (gridY + 0.5) * renderer.gridSize)
            }
        }

    }

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

abstract class BaseCell(currentGridHelper: GridHelper) :
    GridCell(currentGridHelper.mouseGridX(), currentGridHelper.mouseGridY(), currentGridHelper.toolRotation) {
    override fun render(now: Long, gc: GraphicsContext, renderer: GridRenderer<out GridCell>) {
        getRenderer().draw(gridX, gridY, gc, renderer, this)
    }

    abstract fun getRenderer(): GridCellRenderer

    open fun getContextOptions(): List<MenuItem> = emptyList()

    open fun getParameters(): Map<String, Property<*>> = emptyMap()

}

class StraightGridCell(gridHelper: GridHelper) : BaseCell(gridHelper) {
    override fun getRenderer() = GridCellRenderer.STRAIGHT
}

class TurnGridCell(gridHelper: GridHelper) : BaseCell(gridHelper) {
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

class TurnoutGridCell(gridHelper: GridHelper, private val turnoutType: TurnoutType) : BaseCell(gridHelper), Consumer<Boolean> {
    var turned = false
    val id = SimpleIntegerProperty(0)

    override fun accept(t: Boolean) {
        turned = t
    }

    init {
        /*ApplicationContext.get().connectionHandler.addTurnout(id.get(), onPacket)
        id.addListener { _, oldValue, newValue ->
            ApplicationContext.get().connectionHandler.removeTurnout(oldValue.toInt(), onPacket)
            ApplicationContext.get().connectionHandler.addTurnout(newValue.toInt(), onPacket)
        }*/
    }

    fun init() {
        val connectionHandler = ApplicationContext.get().connectionHandler
        connectionHandler.addTurnout(id.get(), this)
        id.addListener { _, oldValue, newValue ->
            connectionHandler.removeTurnout(oldValue.toInt(), this)
            connectionHandler.addTurnout(newValue.toInt(), this)
        }
    }

    override fun getRenderer() = turnoutType.getRenderer()

    override fun getContextOptions(): List<MenuItem> {
        val item = MenuItem("Turn")
        item.setOnAction {
            this.turned = !this.turned
        }
        return listOf(item)
    }

    override fun getParameters() = mapOf(Pair("id", id))
}

class TextGridCell(gridHelper: GridHelper) : BaseCell(gridHelper) {
    val fontSize = SimpleIntegerProperty(10)
    val text = SimpleStringProperty("")

    override fun getRenderer() = GridCellRenderer.TEXT

    override fun getParameters(): Map<String, Property<*>> = mapOf(Pair("text", text), Pair("size", fontSize))
}

