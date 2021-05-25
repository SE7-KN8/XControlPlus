package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.collections.FXCollections
import javafx.collections.ObservableList


class Project(var name: String) {

    val sheets: ObservableList<Sheet> = FXCollections.observableArrayList()

    fun newSheet(it: String) {
        sheets.add(Sheet(it, GridView<BaseCell>()))
    }

}
