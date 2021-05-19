package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.actions.Actions
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseEvent

class GridContextMenu(private val state: GridState) {

    private val menu = ContextMenu()

    private val defaultItems = ArrayList<MenuItem>()

    init {
        val rotateCW = MenuItem("Rotate").apply {
            setOnAction { state.doAction(Actions.SelectedCell.rotateClockwise) }
            accelerator = KeyCombination.keyCombination("r")
        }
        val rotateCCW = MenuItem("Inverse Rotate").apply {
            setOnAction { state.doAction(Actions.SelectedCell.rotateCounterClockwise) }
            accelerator = KeyCombination.keyCombination("shift+r")
        }
        val delete = MenuItem("Delete").apply {
            setOnAction { state.doAction(Actions.SelectedCell.delete) }
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
        val options = selectedCell.getContextOptions()
        if (options.isNotEmpty()) {
            menu.items.add(SeparatorMenuItem())
            menu.items.addAll(selectedCell.getContextOptions())
        }
        menu.show(state.gridView, event.screenX, event.screenY)
    }

    fun hide() {
        menu.hide()
    }

}
