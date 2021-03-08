package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.GRID_SIZE

enum class GridCellRenderer {
    STRAIGHT {
        override fun render(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext) {
            gc.fill = Colors.track
            when (rot) {
                Rotation.D0, Rotation.D180 -> {
                    gc.fillRect(
                        gridX.toDouble() * GRID_SIZE,
                        (gridY.toDouble() + 0.3) * GRID_SIZE, GRID_SIZE.toDouble(), 0.4 * GRID_SIZE.toDouble()
                    )
                    gc.fill = Colors.trackHighlight
                    gc.fillRect(
                        gridX.toDouble() * GRID_SIZE,
                        (gridY.toDouble() + 0.4) * GRID_SIZE, GRID_SIZE.toDouble(), 0.2 * GRID_SIZE.toDouble()
                    )
                }
                Rotation.D90, Rotation.D270 -> {
                    gc.fillRect(
                        (gridX.toDouble() + 0.3) * GRID_SIZE,
                        gridY.toDouble() * GRID_SIZE, 0.4 * GRID_SIZE.toDouble(), GRID_SIZE.toDouble()
                    )
                    gc.fill = Colors.trackHighlight
                    gc.fillRect(
                        (gridX.toDouble() + 0.4) * GRID_SIZE,
                        gridY.toDouble() * GRID_SIZE, 0.2 * GRID_SIZE.toDouble(), GRID_SIZE.toDouble()
                    )
                }
            }
        }

    },

    TURN {
        override fun render(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext) {
            TODO("Not yet implemented")
        }

    },

    TURNOUT {
        override fun render(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext) {
            TODO("Not yet implemented")
        }

    },
    ;


    abstract fun render(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext)

}


interface GridCell {
    fun getGridPosX(): Int
    fun getGridPosY(): Int
    fun getRotation(): Rotation
    fun getRenderer(): GridCellRenderer
}

class StraightGridCell(private val gridX: Int, private val gridY: Int, private val rot: Rotation) : GridCell {
    override fun getGridPosX() = gridX

    override fun getGridPosY() = gridY

    override fun getRotation() = rot

    override fun getRenderer() = GridCellRenderer.STRAIGHT
}
