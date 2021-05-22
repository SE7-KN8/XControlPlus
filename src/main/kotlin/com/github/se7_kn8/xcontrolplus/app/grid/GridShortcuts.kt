package com.github.se7_kn8.xcontrolplus.app.grid


import com.github.se7_kn8.xcontrolplus.app.actions.DeleteSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.actions.EditSelectedCellParameterAction
import com.github.se7_kn8.xcontrolplus.app.actions.RotateSelectedCellAction
import com.github.se7_kn8.xcontrolplus.gridview.RotationDirection
import javafx.scene.input.KeyCode

class GridShortcuts(gridState: GridState) {

    init {
        gridState.gridView.setOnKeyPressed {
            when (it.code) {
                KeyCode.R -> {
                    gridState.selectHoveredCell()
                    val rotationDirection = if (it.isShiftDown) RotationDirection.COUNTER_CLOCKWISE else RotationDirection.CLOCKWISE
                    gridState.userRotation = gridState.userRotation.rotate(rotationDirection)
                    gridState.doAction(RotateSelectedCellAction(rotationDirection))
                }
                KeyCode.DELETE -> {
                    gridState.selectHoveredCell()
                    gridState.doAction(DeleteSelectedCellAction())
                }
                KeyCode.E -> {
                    gridState.selectHoveredCell()
                    gridState.doAction(EditSelectedCellParameterAction())
                }
                else -> {
                    println("Unknown key pressed: ${it.code.name}")
                }
            }
        }
    }

}
