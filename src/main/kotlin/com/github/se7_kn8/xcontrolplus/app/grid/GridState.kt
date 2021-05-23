package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.action.Action
import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.github.se7_kn8.xcontrolplus.gridview.CellRotation
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import com.google.gson.reflect.TypeToken

class GridState(val gridView: GridView<BaseCell>) {

    private val type = object : TypeToken<ArrayList<BaseCell>>() {}.type!!

    val contextMenu = GridContextMenu(this)

    var userRotation = CellRotation.D0

    fun getCells(): List<BaseCell> = gridView.cells

    fun removeCell(baseCell: BaseCell) = gridView.cells.remove(baseCell)


    fun addCell(baseCell: BaseCell) {
        getCell(baseCell.gridX, baseCell.gridY).ifPresent {
            removeCell(it)
        }
        gridView.cells.add(baseCell)
    }

    fun getCell(x: Int, y: Int) = gridView.findCell(x, y)

    fun getHoveredCell() = gridView.findCell(mouseGridX(), mouseGridY())

    fun getSelectedCell(): BaseCell? = gridView.selectedCell

    fun mouseGridX() = gridView.mouseGridX
    fun mouseGridY() = gridView.mouseGridY

    fun loadCells(from: String) {
        val newData = ApplicationContext.get().gson.fromJson<ArrayList<BaseCell>>(from, type)
        gridView.cells.clear()
        gridView.cells.addAll(newData)
    }

    fun saveCells(): String {
        val data: ArrayList<BaseCell> = ArrayList(gridView.cells)
        return ApplicationContext.get().gson.toJson(data, type)
    }

    fun doAction(action: Action) {
        action.init(this)
        if (action.valid(this)) {
            action.doAction(this)
        }
    }

    fun saveToFile() {
        FileUtil.saveFileChooser(FileUtil.PROJECT_FILE) {
            val content = saveCells()
            FileUtil.writeStringToFile(it, content)
        }
    }

    fun loadFromFile() {
        FileUtil.openFileChooser(FileUtil.PROJECT_FILE) { path ->
            FileUtil.readFileToString(path)?.also {
                loadCells(it)
            }

        }
    }

    fun selectHoveredCell() {
        val cell = gridView.findCell(gridView.mouseGridX, gridView.mouseGridY)
        if (cell.isPresent) {
            gridView.selectedCell = cell.get()
        } else {
            gridView.selectedCell = null
        }
    }

}
