package com.github.se7_kn8.xcontrolplus.app.grid.toolbox

import com.github.se7_kn8.xcontrolplus.app.GRID_SIZE
import com.github.se7_kn8.xcontrolplus.app.grid.*
import javafx.scene.Cursor
import javafx.scene.canvas.GraphicsContext

enum class ToolboxMode {

    MOUSE {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            // Do nothing
        }

        override fun getCursor(): Cursor = Cursor.DEFAULT

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            for (cell in cells) {
                if (cell.getGridPosX() == gridX && cell.getGridPosY() == gridY && cell is TurnoutGridCell) {
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

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
            cells.add(StraightGridCell(gridX, gridY, rot))
        }
    },
    TURN {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            GridCellRenderer.TURN.draw(gridX, gridY, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
            cells.add(TurnGridCell(gridX, gridY, rot))
        }
    },

    LEFT_TURNOUT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            GridCellRenderer.LEFT_TURNOUT.draw(gridX, gridY, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
            cells.add(TurnoutGridCell(gridX, gridY, rot, TurnoutType.LEFT))
        }
    },

    RIGHT_TURNOUT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext) {
            GridCellRenderer.RIGHT_TURNOUT.draw(gridX, gridY, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
            cells.add(TurnoutGridCell(gridX, gridY, rot, TurnoutType.RIGHT))
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

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
        }

    },
    ;

    abstract fun draw(gridX: Int, gridY: Int, gc: GraphicsContext)

    abstract fun getCursor(): Cursor

    abstract fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>)

    fun removeCellAtSamePos(gridX: Int, gridY: Int, cells: ArrayList<GridCell>) {
        var foundCell: GridCell? = null
        for (cell in cells) {
            if (cell.getGridPosX() == gridX && cell.getGridPosY() == gridY) {
                foundCell = cell
                break
            }
        }
        cells.remove(foundCell)
    }

}
