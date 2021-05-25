package com.github.se7_kn8.xcontrolplus.app.toolbox

import com.github.se7_kn8.xcontrolplus.app.action.AddCellAction
import com.github.se7_kn8.xcontrolplus.app.action.DeleteSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.grid.*
import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.scene.Cursor
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

enum class Tool(val cursor: Cursor = Cursor.DEFAULT, val allowDrag: Boolean = false) {

    MOUSE {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            // Do nothing
        }

        override fun onClick(event: MouseEvent, helper: GridHelper) {
            when (event.button) {
                MouseButton.PRIMARY -> {
                }

                MouseButton.SECONDARY -> {
                    helper.getHoveredCell().ifPresent {
                        helper.contextMenu.show(event, it)
                    }
                }
                else -> {
                    //NOP
                }
            }
        }

        override fun getImage() = FileUtil.getImage("tool/cursor.png")

    },
    STRAIGHT(Cursor.CROSSHAIR, true) {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.STRAIGHT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun onClick(event: MouseEvent, helper: GridHelper) {
            if (event.button == MouseButton.PRIMARY) {
                helper.doAction(AddCellAction(StraightGridCell(helper)))
            }
        }

        override fun getImage() = FileUtil.getImage("tool/straight.png")
    },
    TURN(Cursor.CROSSHAIR, true) {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.TURN.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun onClick(event: MouseEvent, helper: GridHelper) {
            if (event.button == MouseButton.PRIMARY) {
                helper.doAction(AddCellAction(TurnGridCell(helper)))
            }
        }

        override fun getImage() = FileUtil.getImage("tool/turn.png")
    },

    LEFT_TURNOUT(Cursor.CROSSHAIR) {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.LEFT_TURNOUT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun onClick(event: MouseEvent, helper: GridHelper) {
            if (event.button == MouseButton.PRIMARY) {
                helper.doAction(AddCellAction(TurnoutGridCell(helper, TurnoutType.LEFT)))
            }
        }

        override fun getImage() = FileUtil.getImage("tool/left_turnout.png")
    },
    RIGHT_TURNOUT(Cursor.CROSSHAIR) {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.RIGHT_TURNOUT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun onClick(event: MouseEvent, helper: GridHelper) {
            if (event.button == MouseButton.PRIMARY) {
                helper.doAction(AddCellAction(TurnoutGridCell(helper, TurnoutType.RIGHT)))
            }
        }

        override fun getImage() = FileUtil.getImage("tool/right_turnout.png")
    },

    TEXT(Cursor.CROSSHAIR) {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.TEXT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun onClick(event: MouseEvent, helper: GridHelper) {
            if (event.button == MouseButton.PRIMARY) {
                helper.doAction(AddCellAction(TextGridCell(helper)))
            }
        }

        override fun getImage()= FileUtil.getImage("tool/text.png")
    },

    DELETE(Cursor.OPEN_HAND, true) {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            gc.fill = Colors.track
            gc.fillRect(
                gridX * gridView.gridSize, gridY * gridView.gridSize, gridView.gridSize, gridView.gridSize
            )
        }

        override fun onClick(event: MouseEvent, helper: GridHelper) {
            if (event.button == MouseButton.PRIMARY) {
                helper.selectHoveredCell()
                helper.doAction(DeleteSelectedCellAction())
            }
        }

        override fun getImage()= FileUtil.getImage("tool/delete.png")
    },
    ;

    abstract fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>)

    abstract fun onClick(event: MouseEvent, helper: GridHelper)

    abstract fun getImage(): Image

}
