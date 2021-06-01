package com.github.se7_kn8.xcontrolplus.app.grid


import com.github.se7_kn8.xcontrolplus.app.action.DeleteSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.action.EditSelectedCellParameterAction
import com.github.se7_kn8.xcontrolplus.app.action.RotateSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.toolbox.Tool
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolRenderer
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.gridview.RotationDirection
import javafx.scene.input.KeyCode

class GridShortcuts(private val toolRenderer: ToolRenderer) {

    fun detach(helper: GridHelper) {
        helper.gridView.onKeyPressed = null
    }

    fun attach(helper: GridHelper) {
        helper.gridView.isFocusTraversable = true
        helper.gridView.setOnKeyPressed {
            when (it.code) {
                KeyCode.R -> {
                    val rotationDirection = if (it.isShiftDown) RotationDirection.COUNTER_CLOCKWISE else RotationDirection.CLOCKWISE
                    // Only rotate the cell if the current tool is mouse, else rotate the tool
                    if (toolRenderer.currentTool.get() == Tool.MOUSE) {
                        helper.selectHoveredCell()
                        helper.doAction(RotateSelectedCellAction(rotationDirection))
                    } else {
                        helper.toolRotation = helper.toolRotation.rotate(rotationDirection)
                    }

                }
                KeyCode.DELETE -> {
                    helper.selectHoveredCell()
                    helper.doAction(DeleteSelectedCellAction())
                }
                KeyCode.E -> {
                    helper.selectHoveredCell()
                    helper.doAction(EditSelectedCellParameterAction())
                }
                KeyCode.ESCAPE -> {
                    if (toolRenderer.currentTool.get() != Tool.MOUSE) {
                        toolRenderer.currentTool.value = Tool.MOUSE
                    }
                }
                else -> {
                    debug("Unknown key pressed: ${it.code.name}")
                }
            }
        }
    }

}
