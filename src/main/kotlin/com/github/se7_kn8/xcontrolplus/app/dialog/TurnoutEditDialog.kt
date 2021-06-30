package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.project.turnout.Turnout
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Callback
import kotlin.math.pow

enum class InputState(val color: Color) {
    TURNED(Color.RED),
    NOT_TURNED(Color.LIME)
}

class TurnoutEditDialog<OutputState : Enum<*>>(private val turnout: Turnout<OutputState>) :
    Alert(AlertType.NONE, "", ButtonType.OK, ButtonType.CANCEL),
    AppDialog<Unit> {

    inner class OutputStateCellFactory : Callback<ListView<OutputState>, ListCell<OutputState>> {

        override fun call(param: ListView<OutputState>?): ListCell<OutputState> {
            return object : ListCell<OutputState>() {
                override fun updateItem(item: OutputState?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        graphic = null
                    } else {
                        // TODO add graphics
                        text = translate("turnout.state.${item.name.lowercase()}")
                    }
                }
            }
        }
    }

    private val root = GridPane()
    private val comboBoxMap = HashMap<Int, ComboBox<OutputState>>()
    private val maxOutputs: Int

    init {
        initOwner(WindowContext.get().primaryStage)
        title = translate("dialog.edit_turnout")
        headerText = translate("dialog.edit_turnout")

        root.isGridLinesVisible = true

        turnout.getAddresses().forEachIndexed { index, it ->
            root.add(Label(translate("dialog.edit_turnout.input", it)).apply { padding = Insets(10.0) }, 1 + index, 0)
        }

        maxOutputs = (2.0.pow(turnout.logic.possibleInputs)).toInt()

        for (i in 1..maxOutputs) {
            val defaultValue = turnout.stateMap.getOrElse(i - 1) { turnout.logic.possibleOutputs.first() }
            val box = createStateBox(defaultValue)
            comboBoxMap[i - 1] = box
            root.add(box, 0, i)
        }


        for ((counter, row) in (1..maxOutputs).withIndex()) {
            for (col in 1..turnout.logic.possibleInputs) {
                val bitAtPos = ((counter shr (col - 1)) and 0x1) == 1
                val state = if (bitAtPos) {
                    InputState.TURNED
                } else {
                    InputState.NOT_TURNED
                }
                root.add(Label(translate("dialog.edit_turnout.${state.name.lowercase()}"), Rectangle(20.0, 20.0, state.color)).apply {
                    padding = Insets(5.0)
                }, col, row)
            }
        }

        dialogPane.content = root
    }

    private fun createStateBox(defaultValue: OutputState): ComboBox<OutputState> {
        val box = ComboBox(FXCollections.observableArrayList(turnout.logic.possibleOutputs.toList()))
        box.selectionModel.select(defaultValue)
        box.cellFactory = OutputStateCellFactory()
        box.buttonCell = box.cellFactory.call(null)
        return box
    }

    override fun showDialog() {
        val buttonType = showAndWait()
        if (buttonType.get() == ButtonType.OK) {
            val map = HashMap<Int, OutputState>()
            for (row in 0 until maxOutputs) {
                map[row] = comboBoxMap[row]!!.value
            }
            turnout.stateMap = map
        }
    }
}
