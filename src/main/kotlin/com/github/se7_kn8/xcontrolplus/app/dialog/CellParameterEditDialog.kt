package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.StringProperty
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane

class CellParameterEditDialog(private val cell: BaseCell) : Alert(AlertType.NONE, "", ButtonType.OK), AppDialog<Unit> {

    init {
        initOwner(WindowContext.get().primaryStage)
        val root = GridPane()

        var pos = 0
        cell.getParameters().forEach { parameter ->
            val node: Node = when (val property = parameter.value) {
                is StringProperty -> {
                    TextField(property.get()).apply {
                        property.bind(this.textProperty())
                    }
                }
                is IntegerProperty -> {
                    // TODO this could be better because this field accepts non digit characters and throws error. maybe TextFormatter?
                    Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, property.get()).apply {
                        isEditable = true
                        property.bind(this.valueProperty())
                    }
                }
                is BooleanProperty -> {
                    CheckBox().apply {
                        isSelected = property.get()
                        property.bind(this.selectedProperty())
                    }
                }
                else -> {
                    Label(translate("dialog.parameter.unknown"))
                }
            }
            root.add(Label(translate("parameter." + parameter.key.toLowerCase())), 0, pos)
            root.add(node, 1, pos)
            pos += 1
        }

        title = translate("dialog.parameter")
        dialogPane.headerText = translate("dialog.parameter")
        dialogPane.content = root
    }

    override fun showDialog() {
        this.showAndWait()
        cell.getParameters().values.forEach { property ->
            property.unbind()
        }
    }

}
