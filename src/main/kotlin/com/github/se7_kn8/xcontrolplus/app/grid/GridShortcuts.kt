package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.actions.Actions
import javafx.scene.input.KeyCode

class GridShortcuts(gridState: GridState) {

    init {
        gridState.gridView.setOnKeyPressed {
            when (it.code) {
                KeyCode.R -> {
                    if (it.isShiftDown) {
                        gridState.doAction(Actions.SelectedCell.rotateCounterClockwise)
                    } else {
                        gridState.doAction(Actions.SelectedCell.rotateClockwise)
                    }
                }
                KeyCode.DELETE -> {
                    gridState.doAction(Actions.SelectedCell.delete)
                }
                else -> {
                    println("Unknown key pressed: ${it.code.name}")
                }
            }
        }
    }

}
