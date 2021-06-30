package com.github.se7_kn8.xcontrolplus.app.action

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.dialog.CellParameterEditDialog
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridHelper
import com.github.se7_kn8.xcontrolplus.app.grid.TurnoutGridCell
import com.github.se7_kn8.xcontrolplus.app.project.turnout.Turnout
import com.github.se7_kn8.xcontrolplus.gridview.RotationDirection

interface Action {
    fun init(helper: GridHelper)
    fun valid(helper: GridHelper): Boolean
    fun doAction(helper: GridHelper)
    fun undoAction(helper: GridHelper)
}

abstract class SelectedCellAction : Action {
    protected var cell: BaseCell? = null
    override fun init(helper: GridHelper) {
        cell = helper.getSelectedCell()
    }

    override fun valid(helper: GridHelper) = cell != null
}

class RotateSelectedCellAction(private val direction: RotationDirection) : SelectedCellAction() {

    override fun doAction(helper: GridHelper) {
        cell?.rotation = cell?.rotation?.rotate(direction)
    }

    override fun undoAction(helper: GridHelper) {
        cell?.rotation = cell?.rotation?.rotate(direction.invert())
    }
}

class DeleteSelectedCellAction : SelectedCellAction() {
    override fun doAction(helper: GridHelper) {
        cell?.let {
            helper.removeCell(it)
            if (it is Turnout<*>) {
                ApplicationContext.get().connectionHandler.removeTurnout(it)
            }
        }
        helper.selectHoveredCell()
    }

    override fun undoAction(helper: GridHelper) {
        cell?.let {
            helper.addCell(it)
            if (it is Turnout<*>) {
                ApplicationContext.get().connectionHandler.addTurnout(it)
            }
        }
    }

}

class AddCellAction(private val cell: BaseCell) : Action {

    override fun init(helper: GridHelper) {
        if (cell.getParameters().keys.isNotEmpty()) {
            CellParameterEditDialog(cell).showAndWait()
        }
        cell.setPosFromMouse(helper)
    }

    override fun valid(helper: GridHelper) = true

    override fun doAction(helper: GridHelper) {
        helper.addCell(cell)
        if (cell is TurnoutGridCell) {
            cell.init()
        }
    }

    override fun undoAction(helper: GridHelper) {
        helper.removeCell(cell)
        helper.selectHoveredCell()
    }

}

class EditSelectedCellParameterAction : SelectedCellAction() {

    override fun valid(helper: GridHelper) = super.valid(helper) && cell?.getParameters()?.keys?.isNotEmpty() == true

    override fun doAction(helper: GridHelper) {
        cell?.let {
            CellParameterEditDialog(it).showDialog()
        }
    }

    override fun undoAction(helper: GridHelper) {
        TODO("Not yet implemented")
    }

}
