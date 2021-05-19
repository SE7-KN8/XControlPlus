package com.github.se7_kn8.xcontrolplus.app.actions

import com.github.se7_kn8.xcontrolplus.app.grid.GridState

fun interface Action {
    fun doAction(state: GridState)
}

object Actions {


    object SelectedCell {

        val rotateClockwise = Action { state ->
            state.getSelectedCell().ifPresent {
                it.rotation = it.rotation.rotateCW()
            }
        }

        val rotateCounterClockwise = Action { state ->
            state.getSelectedCell().ifPresent {
                it.rotation = it.rotation.rotateCCW()
            }
        }

        val delete = Action { state ->
            state.getSelectedCell().ifPresent {
                state.removeCell(it)
            }
        }
    }

}
