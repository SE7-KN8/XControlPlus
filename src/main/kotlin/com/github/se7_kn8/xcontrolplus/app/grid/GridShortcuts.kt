package com.github.se7_kn8.xcontrolplus.app.grid


import com.github.se7_kn8.xcontrolplus.app.action.DeleteSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.action.EditSelectedCellParameterAction
import com.github.se7_kn8.xcontrolplus.app.action.RotateSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolRenderer
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolboxMode
import com.github.se7_kn8.xcontrolplus.gridview.RotationDirection
import javafx.scene.input.KeyCode

class GridShortcuts(gridState: GridState, toolRenderer: ToolRenderer) {

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
                KeyCode.ESCAPE -> {
                    if (toolRenderer.currentTool.get() != ToolboxMode.MOUSE) {
                        toolRenderer.currentTool.value = ToolboxMode.MOUSE
                    }
                }
                else -> {
                    println("Unknown key pressed: ${it.code.name}")
                }
            }
        }
    }

}
