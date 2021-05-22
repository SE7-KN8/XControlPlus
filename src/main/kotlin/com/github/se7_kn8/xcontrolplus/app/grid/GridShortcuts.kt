package com.github.se7_kn8.xcontrolplus.app.grid


import com.github.se7_kn8.xcontrolplus.app.actions.DeleteSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.actions.RotateSelectedCellAction
import com.github.se7_kn8.xcontrolplus.gridview.RotationDirection
import javafx.scene.input.KeyCode

class GridShortcuts(gridState: GridState) {

    init {
        gridState.gridView.setOnKeyPressed {
            when (it.code) {
                KeyCode.R -> {
                    gridState.doAction(RotateSelectedCellAction(if (it.isShiftDown) RotationDirection.COUNTER_CLOCKWISE else RotationDirection.CLOCKWISE))
                }
                KeyCode.DELETE -> {
                    gridState.doAction(DeleteSelectedCellAction())
                }
                else -> {
                    println("Unknown key pressed: ${it.code.name}")
                }
            }
        }
    }

}
