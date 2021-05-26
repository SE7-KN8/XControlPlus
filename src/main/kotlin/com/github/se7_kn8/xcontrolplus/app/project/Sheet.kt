package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridHelper
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.beans.property.SimpleStringProperty

private data class SaveWrapper(
    val name: String,
    val grid_translation_x: Double,
    val grid_translation_y: Double,
    val grid_scale: Double,
    val cells: ArrayList<BaseCell>
)

class Sheet(name: String, gridView: GridView<BaseCell>) {

    val gridHelper = GridHelper(gridView)
    val name = SimpleStringProperty(name)

    fun save(): String {
        val data: ArrayList<BaseCell> = ArrayList(gridHelper.getCells())
        return ApplicationContext.get().gson.toJson(
            SaveWrapper(
                name.get(),
                gridHelper.gridView.translationX,
                gridHelper.gridView.translationY,
                gridHelper.gridView.scale,
                data
            )
        )
    }

    companion object {
        fun load(from: String): Sheet {
            val wrapper = ApplicationContext.get().gson.fromJson(from, SaveWrapper::class.java)
            val sheet = Sheet(wrapper.name, GridView<BaseCell>().apply {
                // This needs to be happen in this order, because the gridview recalculates the translation based on the scale and we want the saved translation to be applied
                scale = wrapper.grid_scale
                translationX = wrapper.grid_translation_x
                translationY = wrapper.grid_translation_y
            })
            sheet.gridHelper.addCells(wrapper.cells)
            return sheet
        }
    }
}
