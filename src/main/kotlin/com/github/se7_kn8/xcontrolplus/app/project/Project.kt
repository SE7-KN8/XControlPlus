package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.collections.FXCollections
import javafx.collections.ObservableList


class Project(var name: String) {

    val sheets: ObservableList<Sheet> = FXCollections.observableArrayList()

    fun newSheet(name: String) {
        debug("Adding new sheet '$name' to project '${this.name}'")
        sheets.add(Sheet(name, GridView<BaseCell>()))
    }

    fun init() {
        sheets.forEach(Sheet::init)
    }

}
