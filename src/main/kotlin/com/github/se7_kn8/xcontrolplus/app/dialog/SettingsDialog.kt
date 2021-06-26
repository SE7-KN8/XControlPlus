package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.settings.SettingsEntry
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.event.ActionEvent
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority

// A dialog for the user settings
class SettingsDialog : Dialog<ButtonType>(), AppDialog<Unit> {

    private val settingsMap = HashMap<SettingsEntry<*>, Any>()

    init {
        initOwner(WindowContext.get().primaryStage)
        headerText = translate("dialog.settings")
        title = translate("dialog.settings")

        var rowCounter = 0

        val root = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            maxWidth = Double.MAX_VALUE
            alignment = Pos.CENTER_LEFT
        }
        for (key in ApplicationContext.get().userSettings.entries) {
            val value = ApplicationContext.get().userSettings.getAny(key)
            settingsMap[key] = value


            val node: Node = when (value) {
                is Boolean -> {
                    CheckBox().apply {
                        isSelected = value
                        selectedProperty().addListener { _, _, newValue ->
                            settingsMap[key] = newValue
                        }
                    }
                }
                is String -> {
                    TextField().apply {
                        text = value
                        textProperty().addListener { _, _, newValue ->
                            settingsMap[key] = newValue
                        }
                    }
                }
                // TODO add more
                else -> {
                    Label(translate("dialog.settings.unknown"))
                }
            }
            GridPane.setHalignment(node, HPos.RIGHT)
            GridPane.setHgrow(node, Priority.ALWAYS)
            root.add(Label(translate("setting.${key.saveName}")), 0, rowCounter)
            root.add(node, 1, rowCounter)
            rowCounter += 1
        }

        dialogPane.content = root
        dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.OK, ButtonType.CANCEL)

        // Don't close the dialog when the user clicks on Apply
        dialogPane.lookupButton(ButtonType.APPLY).addEventFilter(ActionEvent.ACTION) {
            it.consume()
            applySettings()
        }
    }

    override fun showDialog() {
        showAndWait().ifPresent {
            if (it == ButtonType.OK) {
                applySettings()
            }
        }
    }


    private fun applySettings() {
        settingsMap.forEach { (key, value) ->
            ApplicationContext.get().userSettings.setAny(key, value)
        }
        ApplicationContext.get().userSettings.save()
    }

}
