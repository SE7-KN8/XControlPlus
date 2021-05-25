package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridHelper
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.beans.property.SimpleStringProperty

private data class SaveWrapper(val name: String, val cells: ArrayList<BaseCell>)

class Sheet(name: String, gridView: GridView<BaseCell>) {

    val gridHelper = GridHelper(gridView)
    val name = SimpleStringProperty(name)

    fun save(): String {
        val data: ArrayList<BaseCell> = ArrayList(gridHelper.getCells())
        return ApplicationContext.get().gson.toJson(SaveWrapper(name.get(), data))
    }

    companion object {
        fun load(from: String): Sheet {
            val wrapper = ApplicationContext.get().gson.fromJson(from, SaveWrapper::class.java)
            val sheet = Sheet(wrapper.name, GridView<BaseCell>())
            sheet.gridHelper.addCells(wrapper.cells)
            return sheet
        }
    }
}
