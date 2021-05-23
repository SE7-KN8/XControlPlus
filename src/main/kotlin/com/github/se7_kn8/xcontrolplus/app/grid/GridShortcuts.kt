package com.github.se7_kn8.xcontrolplus.app.grid


import com.github.se7_kn8.xcontrolplus.app.action.DeleteSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.action.EditSelectedCellParameterAction
import com.github.se7_kn8.xcontrolplus.app.action.RotateSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.toolbox.Tool
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolRenderer
import com.github.se7_kn8.xcontrolplus.gridview.RotationDirection
import javafx.scene.input.KeyCode

class GridShortcuts(gridState: GridState, toolRenderer: ToolRenderer) {

    init {
        gridState.gridView.setOnKeyPressed {
            when (it.code) {
                KeyCode.R -> {
                    val rotationDirection = if (it.isShiftDown) RotationDirection.COUNTER_CLOCKWISE else RotationDirection.CLOCKWISE
                    // Only rotate the cell if the current tool is mouse, else rotate the tool
                    if (toolRenderer.currentTool.get() == Tool.MOUSE) {
                        gridState.selectHoveredCell()
                        gridState.doAction(RotateSelectedCellAction(rotationDirection))
                    } else {
                        gridState.toolRotation = gridState.toolRotation.rotate(rotationDirection)
                    }

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
                    if (toolRenderer.currentTool.get() != Tool.MOUSE) {
                        toolRenderer.currentTool.value = Tool.MOUSE
                    }
                }
                else -> {
                    println("Unknown key pressed: ${it.code.name}")
                }
            }
        }
    }
}
