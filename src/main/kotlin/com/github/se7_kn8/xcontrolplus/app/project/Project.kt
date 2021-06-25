package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.dialog.ProjectUpdateDialog
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.application.Platform
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
        Platform.runLater { updateTurnoutStates() }
    }

    fun updateTurnoutStates() {
        if (ApplicationContext.get().connectionHandler.hasConnection()) {
            ProjectUpdateDialog(this).showDialog()
        }
    }

    fun getTurnoutAddresses(): List<Int> {
        val addresses = sheets.map { it.getTurnoutAddresses() }
        return addresses.flatten().distinct()
    }


}
