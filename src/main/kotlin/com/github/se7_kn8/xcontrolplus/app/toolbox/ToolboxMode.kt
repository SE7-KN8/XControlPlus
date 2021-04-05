package com.github.se7_kn8.xcontrolplus.app.toolbox

import com.github.se7_kn8.xcontrolplus.app.grid.*
import com.github.se7_kn8.xcontrolplus.gridview.CellRotation
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.scene.Cursor
import javafx.scene.canvas.GraphicsContext

enum class ToolboxMode {

    MOUSE {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            // Do nothing
        }

        override fun getCursor(): Cursor = Cursor.DEFAULT

        override fun onClick(gridX: Int, gridY: Int, rot: CellRotation, state: GridState) {
            for (cell in state.getCells()) {
                if (cell.gridX == gridX && cell.gridY == gridY && cell is TurnoutGridCell) {
                    cell.turned = !cell.turned
                }
            }
        }
    },
    STRAIGHT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.STRAIGHT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: CellRotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
            state.addCell(StraightGridCell(gridX, gridY, rot))
        }
    },
    TURN {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.TURN.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: CellRotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
            state.addCell(TurnGridCell(gridX, gridY, rot))
        }
    },

    LEFT_TURNOUT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.LEFT_TURNOUT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: CellRotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
            state.addCell(TurnoutGridCell(gridX, gridY, rot, TurnoutType.LEFT))
        }
    },

    RIGHT_TURNOUT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.RIGHT_TURNOUT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: CellRotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
            state.addCell(TurnoutGridCell(gridX, gridY, rot, TurnoutType.RIGHT))
        }
    },

    DELETE {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            gc.fill = Colors.track
            gc.fillRect(
                gridX * gridView.gridSize, gridY * gridView.gridSize, gridView.gridSize, gridView.gridSize
            )
        }

        override fun getCursor(): Cursor = Cursor.OPEN_HAND

        override fun onClick(gridX: Int, gridY: Int, rot: CellRotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
        }

    },
    ;

    abstract fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>)

    abstract fun getCursor(): Cursor

    abstract fun onClick(gridX: Int, gridY: Int, rot: CellRotation, state: GridState)

    fun removeCellAtSamePos(gridX: Int, gridY: Int, state: GridState) {
        var foundCell: BaseCell? = null
        for (cell in state.getCells()) {
            if (cell.gridX == gridX && cell.gridY == gridY) {
                foundCell = cell
                break
            }
        }
        if (foundCell != null) {
            state.removeCell(foundCell)
        }
    }

}
