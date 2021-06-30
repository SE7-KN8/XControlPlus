package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.project.turnout.ThreeWayTurnoutLogic
import com.github.se7_kn8.xcontrolplus.app.project.turnout.ThreeWayTurnoutOutputState
import com.github.se7_kn8.xcontrolplus.app.project.turnout.Turnout
import com.github.se7_kn8.xcontrolplus.app.project.turnout.logic.SimpleTurnoutLogic
import com.github.se7_kn8.xcontrolplus.app.project.turnout.logic.SimpleTurnoutOutputState
import com.github.se7_kn8.xcontrolplus.app.settings.UserSettings
import com.github.se7_kn8.xcontrolplus.app.util.rotated
import com.github.se7_kn8.xcontrolplus.app.util.translate
import com.github.se7_kn8.xcontrolplus.gridview.GridRenderer
import com.github.se7_kn8.xcontrolplus.gridview.model.GridCell
import javafx.beans.property.Property
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.MenuItem
import javafx.scene.text.Font

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
            val colors = ApplicationContext.get().userSettings[UserSettings.COLORED_TURNOUTS]
            if (cell is TurnoutGridCell) {
                when (cell.logic.getState()) {
                    SimpleTurnoutOutputState.TURNED -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                            gc.fill = Colors.turnout
                        }
                        gc.rotated(90.0, renderer.getMidX(gridX.toDouble()), renderer.getMidY(gridY.toDouble())) {
                            TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                        }
                    }
                    SimpleTurnoutOutputState.NOT_TURNED -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            gc.rotated(90.0, renderer.getMidX(gridX.toDouble()), renderer.getMidY(gridY.toDouble())) {
                                TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                            }
                            gc.fill = Colors.turnout
                        }
                        STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                    }
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
                val colors = ApplicationContext.get().userSettings[UserSettings.COLORED_TURNOUTS]
                when (cell.logic.getState()) {
                    SimpleTurnoutOutputState.TURNED -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                            gc.fill = Colors.turnout
                        }
                        TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                    }
                    SimpleTurnoutOutputState.NOT_TURNED -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                            gc.fill = Colors.turnout
                        }
                        STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                    }
                }
            } else {
                STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                TURN.drawForeground(gridX, gridY, gc, renderer, cell)
            }
        }

    },

    Y_TURNOUT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            TURN.drawBackground(gridX, gridY, gc, renderer, cell)
            gc.rotated(
                90.0, renderer.getMidX(gridX.toDouble()),
                renderer.getMidY(gridY.toDouble())
            ) {
                TURN.drawBackground(gridX, gridY, gc, renderer, cell)
            }
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            val colors = ApplicationContext.get().userSettings[UserSettings.COLORED_TURNOUTS]
            if (cell is TurnoutGridCell) {
                when (cell.logic.getState()) {
                    SimpleTurnoutOutputState.TURNED -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                            gc.fill = Colors.turnout
                        }
                        gc.rotated(90.0, renderer.getMidX(gridX.toDouble()), renderer.getMidY(gridY.toDouble())) {
                            TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                        }
                    }
                    SimpleTurnoutOutputState.NOT_TURNED -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            gc.rotated(90.0, renderer.getMidX(gridX.toDouble()), renderer.getMidY(gridY.toDouble())) {
                                TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                            }
                            gc.fill = Colors.turnout
                        }
                        TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                    }
                }
            } else {
                TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                gc.rotated(90.0, renderer.getMidX(gridX.toDouble()), renderer.getMidY(gridY.toDouble())) {
                    TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                }
            }
        }
    },

    THREE_WAY_TURNOUT {
        override fun drawBackground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            Y_TURNOUT.drawBackground(gridX, gridY, gc, renderer, cell)
            STRAIGHT.drawBackground(gridX, gridY, gc, renderer, cell)
        }

        override fun drawForeground(gridX: Int, gridY: Int, gc: GraphicsContext, renderer: GridRenderer<out GridCell>, cell: GridCell?) {
            val colors = ApplicationContext.get().userSettings[UserSettings.COLORED_TURNOUTS]
            if (cell is ThreeWayTurnout) {
                when (cell.logic.getState()) {
                    ThreeWayTurnoutOutputState.NOT_TURNED -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            Y_TURNOUT.drawForeground(gridX, gridY, gc, renderer, cell)
                            gc.fill = Colors.turnout
                        }
                        STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
                    }
                    ThreeWayTurnoutOutputState.TURNED_LEFT -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            RIGHT_TURNOUT.drawForeground(gridX, gridY, gc, renderer, cell)
                            gc.fill = Colors.turnout
                        }
                        gc.rotated(90.0, renderer.getMidX(gridX.toDouble()), renderer.getMidY(gridY.toDouble())) {
                            TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                        }
                    }
                    ThreeWayTurnoutOutputState.TURNED_RIGHT -> {
                        if (colors) {
                            gc.fill = Colors.turnoutBlock
                            LEFT_TURNOUT.drawForeground(gridX, gridY, gc, renderer, cell)
                            gc.fill = Colors.turnout
                        }
                        TURN.drawForeground(gridX, gridY, gc, renderer, cell)
                    }
                }
            } else {
                Y_TURNOUT.drawForeground(gridX, gridY, gc, renderer, cell)
                STRAIGHT.drawForeground(gridX, gridY, gc, renderer, cell)
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

abstract class BaseCell : GridCell() {

    fun setPosFromMouse(gridHelper: GridHelper) {
        gridX = gridHelper.mouseGridX()
        gridY = gridHelper.mouseGridY()
        rotation = gridHelper.toolRotation
    }

    override fun render(now: Long, gc: GraphicsContext, renderer: GridRenderer<out GridCell>) {
        getRenderer().draw(gridX, gridY, gc, renderer, this)
    }

    abstract fun getRenderer(): GridCellRenderer

    open fun getContextOptions(): List<MenuItem> = emptyList()

    open fun getParameters(): Map<String, Property<*>> = emptyMap()

}

class StraightGridCell : BaseCell() {
    override fun getRenderer() = GridCellRenderer.STRAIGHT
}

class TurnGridCell : BaseCell() {
    override fun getRenderer() = GridCellRenderer.TURN
}

enum class TurnoutType {
    LEFT {
        override fun getRenderer() = GridCellRenderer.LEFT_TURNOUT

    },
    RIGHT {
        override fun getRenderer() = GridCellRenderer.RIGHT_TURNOUT
    },
    Y {
        override fun getRenderer() = GridCellRenderer.Y_TURNOUT
    }
    ;

    abstract fun getRenderer(): GridCellRenderer
}

// The default value for the parameter is necessary
// Because with it kotlin will generate a constructor without parameter
// which will be utilized by gson
// without out it there would be no constructor call which will result in values not being initialized
class TurnoutGridCell(private val turnoutType: TurnoutType = TurnoutType.LEFT) : BaseCell(),
    Turnout<SimpleTurnoutOutputState> {

    private val id = SimpleIntegerProperty(0)

    // The should not be serialized, because it could indicate a false state
    @Transient
    override val logic = SimpleTurnoutLogic(this)

    override var stateMap = logic.getDefaultStateMap()

    override fun init() {
        val connectionHandler = ApplicationContext.get().connectionHandler
        connectionHandler.addTurnout(this)
        id.addListener { _, oldValue, _ ->
            connectionHandler.updateTurnout(oldValue.toInt(), this)
        }
    }

    override fun getRenderer() = turnoutType.getRenderer()


    override fun getContextOptions(): List<MenuItem> {
        val item = MenuItem(translate("context_menu.turn"))
        item.setOnAction {
            turnTo(logic.getState().next())
        }
        return listOf(item)
    }

    override fun getParameters() = mapOf(Pair("id", id))

    override fun getAddresses() = intArrayOf(id.get())

    override fun addressToTurnoutInput(address: Int) = 0

    override fun turnoutInputToAddress(input: Int) = id.get()
}

class ThreeWayTurnout : BaseCell(), Turnout<ThreeWayTurnoutOutputState> {

    private val id1 = SimpleIntegerProperty(0)
    private val id2 = SimpleIntegerProperty(0)

    @Transient
    override val logic = ThreeWayTurnoutLogic(this)

    override var stateMap = logic.getDefaultStateMap()

    override fun init() {
        val connectionHandler = ApplicationContext.get().connectionHandler
        connectionHandler.addTurnout(this)
        id1.addListener { _, oldValue, _ ->
            connectionHandler.updateTurnout(oldValue.toInt(), this)
        }
        id2.addListener { _, oldValue, _ ->
            connectionHandler.updateTurnout(oldValue.toInt(), this)
        }
    }

    override fun getContextOptions(): List<MenuItem> {
        val turnNext = MenuItem(translate("context_menu.turn_next"))
        turnNext.setOnAction {
            turnTo(logic.getState().next())
        }

        val turnStraight = MenuItem(translate("context_menu.turn_straight"))
        turnStraight.setOnAction {
            turnTo(ThreeWayTurnoutOutputState.NOT_TURNED)
        }

        val turnLeft = MenuItem(translate("context_menu.turn_left"))
        turnLeft.setOnAction {
            turnTo(ThreeWayTurnoutOutputState.TURNED_LEFT)
        }

        val turnRight = MenuItem(translate("context_menu.turn_right"))
        turnRight.setOnAction {
            turnTo(ThreeWayTurnoutOutputState.TURNED_RIGHT)
        }

        return listOf(turnNext, turnStraight, turnLeft, turnRight)
    }

    override fun getRenderer() = GridCellRenderer.THREE_WAY_TURNOUT

    override fun getAddresses() = intArrayOf(id1.get(), id2.get())

    override fun getParameters() = mapOf(Pair("id_1", id1), Pair("id_2", id2))

    override fun turnoutInputToAddress(input: Int) = if (input == 0) id1.get() else id2.get()

    override fun addressToTurnoutInput(address: Int) = if (address == id1.get()) 0 else 1
}

class TextGridCell : BaseCell() {
    val fontSize = SimpleIntegerProperty(10)
    val text = SimpleStringProperty("")

    override fun getRenderer() = GridCellRenderer.TEXT

    override fun getParameters(): Map<String, Property<*>> = mapOf(Pair("text", text), Pair("size", fontSize))
}

