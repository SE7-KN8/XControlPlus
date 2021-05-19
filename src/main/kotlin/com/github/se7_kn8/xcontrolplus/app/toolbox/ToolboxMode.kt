package com.github.se7_kn8.xcontrolplus.app.toolbox

import com.github.se7_kn8.xcontrolplus.app.actions.Actions
import com.github.se7_kn8.xcontrolplus.app.grid.*
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.scene.Cursor
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

enum class ToolboxMode {

    MOUSE {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            // Do nothing
        }

        override fun onClick(event: MouseEvent, state: GridState) {
            when (event.button) {
                MouseButton.PRIMARY -> {
                }

                MouseButton.SECONDARY -> {
                    state.getHoveredCell().ifPresent {
                        state.contextMenu.show(event, it)
                    }
                }
                else -> {
                    //NOP
                }
            }
        }

        override fun allowDrag() = false

    },
    STRAIGHT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.STRAIGHT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(event: MouseEvent, state: GridState) {
            if (event.button == MouseButton.PRIMARY) {
                state.addCell(StraightGridCell(state))
            }
        }
    },
    TURN {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.TURN.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(event: MouseEvent, state: GridState) {
            if (event.button == MouseButton.PRIMARY) {
                state.addCell(TurnGridCell(state))
            }
        }
    },

    LEFT_TURNOUT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.LEFT_TURNOUT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(event: MouseEvent, state: GridState) {
            if (event.button == MouseButton.PRIMARY) {
                state.addCell(TurnoutGridCell(state, TurnoutType.LEFT))
            }
        }
    },

    RIGHT_TURNOUT {
        override fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>) {
            GridCellRenderer.RIGHT_TURNOUT.draw(gridX, gridY, gc, gridView.renderer)
        }

        override fun getCursor(): Cursor = Cursor.CROSSHAIR

        override fun onClick(event: MouseEvent, state: GridState) {
            if (event.button == MouseButton.PRIMARY) {
                state.addCell(TurnoutGridCell(state, TurnoutType.RIGHT))
            }
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

        override fun onClick(event: MouseEvent, state: GridState) {
            if (event.button == MouseButton.PRIMARY) {
                state.doAction(Actions.SelectedCell.delete)
            }
        }

    },
    ;

    abstract fun draw(gridX: Int, gridY: Int, gc: GraphicsContext, gridView: GridView<BaseCell>)

    abstract fun onClick(event: MouseEvent, state: GridState)

    open fun getCursor(): Cursor = Cursor.DEFAULT

    open fun allowDrag() = true

}
