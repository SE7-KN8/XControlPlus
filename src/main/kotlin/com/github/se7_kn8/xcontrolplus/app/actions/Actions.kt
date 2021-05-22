package com.github.se7_kn8.xcontrolplus.app.actions

import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridState
import com.github.se7_kn8.xcontrolplus.gridview.RotationDirection

interface Action {
    fun init(state: GridState)
    fun valid(state: GridState): Boolean
    fun doAction(state: GridState)
    fun undoAction(state: GridState)
}

abstract class SelectedCellAction : Action {
    protected var cell: BaseCell? = null
    override fun init(state: GridState) {
        cell = state.getSelectedCell()
    }

    override fun valid(state: GridState) = cell != null
}

class RotateSelectedCellAction(private val direction: RotationDirection) : SelectedCellAction() {

    override fun doAction(state: GridState) {
        cell?.rotation = cell?.rotation?.rotate(direction)
    }

    override fun undoAction(state: GridState) {
        cell?.rotation = cell?.rotation?.rotate(direction.invert())
    }
}

class DeleteSelectedCellAction() : SelectedCellAction() {
    override fun doAction(state: GridState) {
        cell?.let {
            state.removeCell(it)
        }
        state.selectHoveredCell()
    }

    override fun undoAction(state: GridState) {
        cell?.let {
            state.addCell(it)
        }
    }

}

class AddCellAction(private val cell: BaseCell) : Action {
    override fun init(state: GridState) {

    }

    override fun valid(state: GridState) = true

    override fun doAction(state: GridState) {
        state.addCell(cell)
    }

    override fun undoAction(state: GridState) {
        state.removeCell(cell)
        state.selectHoveredCell()
    }

}
