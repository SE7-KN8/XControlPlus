package com.github.se7_kn8.xcontrolplus.app.grid.toolbox

import com.github.se7_kn8.xcontrolplus.app.GRID_SIZE
import com.github.se7_kn8.xcontrolplus.app.grid.*
import javafx.scene.Cursor

enum class ToolboxMode {

    MOUSE {
        override fun draw(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext) {
            gc.fillRect(
                (gridX * GRID_SIZE).toDouble(),
                (gridY * GRID_SIZE).toDouble(), GRID_SIZE.toDouble(), GRID_SIZE.toDouble()
            )
        }

        override fun getCursor(): Cursor = Cursor.DEFAULT

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            // Do nothing
        }
    },
    STRAIGHT {
        override fun draw(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext) {
            GridCellRenderer.STRAIGHT.render(gridX, gridY, rot, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
            cells.add(StraightGridCell(gridX, gridY, rot))
        }
    },
    TURN {
        override fun draw(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext) {
            GridCellRenderer.TURN.render(gridX, gridY, rot, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
        }
    },
    TURNOUT {
        override fun draw(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext) {
            GridCellRenderer.TURNOUT.render(gridX, gridY, rot, gc)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
        }
    },

    DELETE {
        override fun draw(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext) {
            // Do nothing
        }

        override fun getCursor(): Cursor = Cursor.OPEN_HAND

        override fun onClick(gridX: Int, gridY: Int, rot: Rotation, cells: ArrayList<GridCell>) {
            removeCellAtSamePos(gridX, gridY, cells)
        }

    },
    ;

    abstract fun draw(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext)

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
