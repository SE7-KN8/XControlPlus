package com.github.se7_kn8.xcontrolplus.app.grid

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.input.MouseEvent

class GridContextMenu(private val state: GridState) {

    private val menu = ContextMenu()

    init {

        val item1 = MenuItem("Test1").apply { setOnAction { println("Test1") } }
        val item2 = MenuItem("Test2").apply { setOnAction { println("Test2") } }
        val item3 = MenuItem("Test3").apply { setOnAction { println("Test3") } }

        menu.items.addAll(item1, item2, item3)

    }

    fun show(event: MouseEvent) {
        menu.hide()
        menu.show(state.gridView, event.screenX, event.screenY)
    }

    fun hide() {
        menu.hide()
    }

}
