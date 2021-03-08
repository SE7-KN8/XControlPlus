package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.GRID_SIZE
import javafx.scene.shape.ArcType

enum class GridCellRenderer {
    STRAIGHT {
        override fun render(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext, cell: GridCell?) {
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
        override fun render(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext, cell: GridCell?) {
            gc.fill = Colors.track

            when (rot) {
                Rotation.D0 -> {
                    var x =
                        doubleArrayOf(
                            (gridX.toDouble() + 0.3) * GRID_SIZE,
                            (gridX.toDouble() + 0.7) * GRID_SIZE,
                            (gridX.toDouble() + 0.0) * GRID_SIZE,
                            (gridX.toDouble() + 0.0) * GRID_SIZE
                        )
                    var y =
                        doubleArrayOf(
                            (gridY.toDouble() + 1.0) * GRID_SIZE,
                            (gridY.toDouble() + 1.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.3) * GRID_SIZE,
                            (gridY.toDouble() + 0.7) * GRID_SIZE
                        )
                    gc.fillPolygon(x, y, 4)
                    gc.fill = Colors.trackHighlight
                    x =
                        doubleArrayOf(
                            (gridX.toDouble() + 0.4) * GRID_SIZE,
                            (gridX.toDouble() + 0.6) * GRID_SIZE,
                            (gridX.toDouble() + 0.0) * GRID_SIZE,
                            (gridX.toDouble() + 0.0) * GRID_SIZE
                        )
                    y =
                        doubleArrayOf(
                            (gridY.toDouble() + 1.0) * GRID_SIZE,
                            (gridY.toDouble() + 1.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.4) * GRID_SIZE,
                            (gridY.toDouble() + 0.6) * GRID_SIZE
                        )
                    gc.fillPolygon(x, y, 4)
                }
                Rotation.D90 -> {
                    var x =
                        doubleArrayOf(
                            (gridX.toDouble() + 0.0) * GRID_SIZE,
                            (gridX.toDouble() + 0.3) * GRID_SIZE,
                            (gridX.toDouble() + 0.7) * GRID_SIZE,
                            (gridX.toDouble() + 0.0) * GRID_SIZE
                        )
                    var y =
                        doubleArrayOf(
                            (gridY.toDouble() + 0.3) * GRID_SIZE,
                            (gridY.toDouble() + 0.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.7) * GRID_SIZE
                        )
                    gc.fillPolygon(x, y, 4)
                    gc.fill = Colors.trackHighlight
                    x =
                        doubleArrayOf(
                            (gridX.toDouble() + 0.0) * GRID_SIZE,
                            (gridX.toDouble() + 0.4) * GRID_SIZE,
                            (gridX.toDouble() + 0.6) * GRID_SIZE,
                            (gridX.toDouble() + 0.0) * GRID_SIZE
                        )
                    y =
                        doubleArrayOf(
                            (gridY.toDouble() + 0.4) * GRID_SIZE,
                            (gridY.toDouble() + 0.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.6) * GRID_SIZE
                        )
                    gc.fillPolygon(x, y, 4)
                }
                Rotation.D180 -> {
                    var x =
                        doubleArrayOf(
                            (gridX.toDouble() + 0.7) * GRID_SIZE,
                            (gridX.toDouble() + 1.0) * GRID_SIZE,
                            (gridX.toDouble() + 1.0) * GRID_SIZE,
                            (gridX.toDouble() + 0.3) * GRID_SIZE
                        )
                    var y =
                        doubleArrayOf(
                            (gridY.toDouble() + 0.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.3) * GRID_SIZE,
                            (gridY.toDouble() + 0.7) * GRID_SIZE,
                            (gridY.toDouble() + 0.0) * GRID_SIZE
                        )
                    gc.fillPolygon(x, y, 4)
                    gc.fill = Colors.trackHighlight
                    x =
                        doubleArrayOf(
                            (gridX.toDouble() + 0.6) * GRID_SIZE,
                            (gridX.toDouble() + 1.0) * GRID_SIZE,
                            (gridX.toDouble() + 1.0) * GRID_SIZE,
                            (gridX.toDouble() + 0.4) * GRID_SIZE
                        )
                    y =
                        doubleArrayOf(
                            (gridY.toDouble() + 0.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.4) * GRID_SIZE,
                            (gridY.toDouble() + 0.6) * GRID_SIZE,
                            (gridY.toDouble() + 0.0) * GRID_SIZE
                        )
                    gc.fillPolygon(x, y, 4)
                }
                Rotation.D270 -> {
                    var x =
                        doubleArrayOf(
                            (gridX.toDouble() + 1.0) * GRID_SIZE,
                            (gridX.toDouble() + 0.7) * GRID_SIZE,
                            (gridX.toDouble() + 0.3) * GRID_SIZE,
                            (gridX.toDouble() + 1.0) * GRID_SIZE
                        )
                    var y =
                        doubleArrayOf(
                            (gridY.toDouble() + 0.7) * GRID_SIZE,
                            (gridY.toDouble() + 1.0) * GRID_SIZE,
                            (gridY.toDouble() + 1.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.3) * GRID_SIZE
                        )
                    gc.fillPolygon(x, y, 4)
                    gc.fill = Colors.trackHighlight
                    x =
                        doubleArrayOf(
                            (gridX.toDouble() + 1.0) * GRID_SIZE,
                            (gridX.toDouble() + 0.6) * GRID_SIZE,
                            (gridX.toDouble() + 0.4) * GRID_SIZE,
                            (gridX.toDouble() + 1.0) * GRID_SIZE
                        )
                    y =
                        doubleArrayOf(
                            (gridY.toDouble() + 0.6) * GRID_SIZE,
                            (gridY.toDouble() + 1.0) * GRID_SIZE,
                            (gridY.toDouble() + 1.0) * GRID_SIZE,
                            (gridY.toDouble() + 0.4) * GRID_SIZE
                        )
                    gc.fillPolygon(x, y, 4)
                    gc.fill = Colors.trackHighlight
                }
            }


        }

    },

    TURNOUT {
        override fun render(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext, cell: GridCell?) {
            if (cell is TurnoutGridCell) {
                if (cell.turned) {
                    STRAIGHT.render(gridX, gridY, rot, gc)
                    TURN.render(gridX, gridY, rot, gc)
                } else {
                    TURN.render(gridX, gridY, rot, gc)
                    STRAIGHT.render(gridX, gridY, rot, gc)
                }
            } else {
                STRAIGHT.render(gridX, gridY, rot, gc)
                TURN.render(gridX, gridY, rot, gc)
            }
        }

    },
    ;


    abstract fun render(gridX: Int, gridY: Int, rot: Rotation, gc: GridContext, cell: GridCell? = null)

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

class TurnGridCell(private val gridX: Int, private val gridY: Int, private val rot: Rotation) : GridCell {
    override fun getGridPosX() = gridX

    override fun getGridPosY() = gridY

    override fun getRotation() = rot

    override fun getRenderer() = GridCellRenderer.TURN
}


class TurnoutGridCell(private val gridX: Int, private val gridY: Int, private val rot: Rotation) : GridCell {

    var turned = false

    override fun getGridPosX() = gridX

    override fun getGridPosY() = gridY

    override fun getRotation() = rot

    override fun getRenderer() = GridCellRenderer.TURNOUT
}

