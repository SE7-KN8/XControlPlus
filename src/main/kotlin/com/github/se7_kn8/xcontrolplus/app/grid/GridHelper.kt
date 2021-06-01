package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.action.Action
import com.github.se7_kn8.xcontrolplus.app.util.trace
import com.github.se7_kn8.xcontrolplus.gridview.CellRotation
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import com.google.gson.reflect.TypeToken

class GridHelper(val gridView: GridView<BaseCell>) {

    private val type = object : TypeToken<ArrayList<BaseCell>>() {}.type!!

    val contextMenu = GridContextMenu(this)

    var toolRotation = CellRotation.D0

    fun getCells(): List<BaseCell> = gridView.cells

    fun removeCell(baseCell: BaseCell) = gridView.cells.remove(baseCell)


    fun addCell(baseCell: BaseCell) {
        getCell(baseCell.gridX, baseCell.gridY).ifPresent {
            trace { "Removing cell $it before adding new cell $baseCell at ${baseCell.gridX}:${baseCell.gridY}" }
            removeCell(it)
        }
        trace { "Add new cell $baseCell at ${baseCell.gridX}:${baseCell.gridY}" }
        gridView.cells.add(baseCell)
    }

    fun addCells(cells: java.util.ArrayList<BaseCell>) {
        trace { "Clearing old cells, before swapping" }
        gridView.cells.clear()
        trace { "Adding all new cells" }
        gridView.cells.addAll(cells)
    }

    fun getCell(x: Int, y: Int) = gridView.findCell(x, y)

    fun getHoveredCell() = gridView.findCell(mouseGridX(), mouseGridY())

    fun getSelectedCell(): BaseCell? = gridView.selectedCell

    fun mouseGridX() = gridView.mouseGridX
    fun mouseGridY() = gridView.mouseGridY

    fun doAction(action: Action) {
        trace { "Init action $action" }
        action.init(this)
        if (action.valid(this)) {
            trace { "Action $action is valid. Executing." }
            action.doAction(this)
        }
    }

    fun selectHoveredCell() {
        val cell = gridView.findCell(gridView.mouseGridX, gridView.mouseGridY)
        if (cell.isPresent) {
            trace { "Select cell $cell at ${gridView.mouseGridX}:${gridView.mouseGridY}" }
            gridView.selectedCell = cell.get()
        } else {
            gridView.selectedCell = null
        }
    }

}
