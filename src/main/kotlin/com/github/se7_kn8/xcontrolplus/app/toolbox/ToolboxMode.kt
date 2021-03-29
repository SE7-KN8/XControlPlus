package com.github.se7_kn8.xcontrolplus.app.toolbox

import com.github.se7_kn8.xcontrolplus.app.grid.*
import javafx.scene.Cursor
import javafx.scene.canvas.GraphicsContext

enum class ToolboxMode {

    MOUSE {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            // Do nothing
        }

        override fun getCursor(): Cursor = Cursor.DEFAULT

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, state: GridState) {
            for (cell in state.cells) {
                if (cell.gridX == gridX && cell.gridY == gridY && cell is TurnoutGridCell) {
                    cell.turned = !cell.turned
                }
            }
        }
    },
    STRAIGHT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            GridCellRenderer.STRAIGHT.draw(gridX, gridY, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
            state.cells.add(StraightGridCell(gridX, gridY, rot))
        }
    },
    TURN {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            GridCellRenderer.TURN.draw(gridX, gridY, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
            state.cells.add(TurnGridCell(gridX, gridY, rot))
        }
    },

    LEFT_TURNOUT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            GridCellRenderer.LEFT_TURNOUT.draw(gridX, gridY, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
            state.cells.add(TurnoutGridCell(gridX, gridY, rot, TurnoutType.LEFT))
        }
    },

    RIGHT_TURNOUT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            GridCellRenderer.RIGHT_TURNOUT.draw(gridX, gridY, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
            state.cells.add(TurnoutGridCell(gridX, gridY, rot, TurnoutType.RIGHT))
        }
    },

    DELETE {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            gc.fillRect(
                (gridX * GRID_SIZE).toDouble(),
                (gridY * GRID_SIZE).toDouble(), GRID_SIZE.toDouble(), GRID_SIZE.toDouble()
            )
        }

        override fun getCursor(): Cursor = Cursor.OPEN_HAND

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, state: GridState) {
            removeCellAtSamePos(gridX, gridY, state)
        }

    },
    ;

    abstract fun draw(gridX: Int, gridY: Int, gc: GraphicsContext)

    abstract fun getCursor(): Cursor

    abstract fun onClick(gridX: Int, gridY: Int, rot: Rotation, state: GridState)

    fun removeCellAtSamePos(gridX: Int, gridY: Int, state: GridState) {
        var foundCell: GridCell? = null
        for (cell in state.cells) {
            if (cell.gridX == gridX && cell.gridY == gridY) {
                foundCell = cell
                break
            }
        }
        state.cells.remove(foundCell)
    }

}
