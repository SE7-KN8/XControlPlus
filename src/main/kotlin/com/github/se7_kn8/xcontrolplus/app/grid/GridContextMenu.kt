package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.action.DeleteSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.action.EditSelectedCellParameterAction
import com.github.se7_kn8.xcontrolplus.app.action.RotateSelectedCellAction
import com.github.se7_kn8.xcontrolplus.app.util.translate
import com.github.se7_kn8.xcontrolplus.gridview.RotationDirection
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseEvent

class GridContextMenu(private val helper: GridHelper) {

    private val menu = ContextMenu()

    private val defaultItems = ArrayList<MenuItem>()

    init {
        val rotateCW = MenuItem(translate("context_menu.rotate")).apply {
            setOnAction { helper.doAction(RotateSelectedCellAction(RotationDirection.CLOCKWISE)) }
            accelerator = KeyCombination.keyCombination("r")
        }
        val rotateCCW = MenuItem(translate("context_menu.inverse_rotate")).apply {
            setOnAction { helper.doAction(RotateSelectedCellAction(RotationDirection.COUNTER_CLOCKWISE)) }
            accelerator = KeyCombination.keyCombination("shift+r")
        }
        val delete = MenuItem(translate("context_menu.delete")).apply {
            setOnAction { helper.doAction(DeleteSelectedCellAction()) }
            accelerator = KeyCombination.keyCombination("delete")
        }

        defaultItems.apply {
            add(rotateCW)
            add(rotateCCW)
            add(delete)
        }

    }

    fun show(event: MouseEvent, selectedCell: BaseCell) {
        menu.hide()
        menu.items.clear()
        menu.items.addAll(defaultItems)

        val parameter = selectedCell.getParameters()
        if (parameter.keys.isNotEmpty()) {
            menu.items.addAll(SeparatorMenuItem())
            menu.items.addAll(MenuItem(translate("context_menu.edit")).apply {
                setOnAction { helper.doAction(EditSelectedCellParameterAction()) }
                accelerator = KeyCombination.keyCombination("e")
            })
        }

        val options = selectedCell.getContextOptions()
        if (options.isNotEmpty()) {
            menu.items.add(SeparatorMenuItem())
            menu.items.addAll(selectedCell.getContextOptions())
        }
        menu.show(helper.gridView, event.screenX, event.screenY)
    }

    fun hide() {
        menu.hide()
    }

}
