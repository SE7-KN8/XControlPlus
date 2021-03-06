package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import com.github.se7_kn8.xcontrolplus.app.util.translate
import com.github.se7_kn8.xcontrolplus.protocol.Connection
import com.github.se7_kn8.xcontrolplus.protocol.ConnectionType
import com.github.se7_kn8.xcontrolplus.protocol.Connections
import javafx.concurrent.Task
import javafx.concurrent.Worker
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.util.StringConverter


class ConnectionTypeStringConverter : StringConverter<ConnectionType>() {
    override fun toString(type: ConnectionType?): String {
        return if (type == null) {
            ""
        } else {
            translate("connection.${type.simpleName}")
        }
    }

    override fun fromString(string: String?) = throw NotImplementedError("Not necessary")
}

class ConnectionStringConverter : StringConverter<Connection>() {
    override fun toString(conn: Connection?): String {
        return if (conn == null) {
            ""
        } else {
            "${conn.fullName} (${conn.simpleName})"
        }
    }

    override fun fromString(string: String?) = throw NotImplementedError("Not necessary")

}

private class ListConnectionsTask(private val connectionType: ConnectionType) : Task<List<Connection>>() {
    override fun call(): List<Connection> {
        return connectionType.listConnections()
    }
}

private class TestConnectionTask(private val connection: Connection) : Task<Boolean>() {
    override fun call(): Boolean {
        connection.openConnection()
        val result = connection.testConnection(5000)
        connection.closeConnection()
        return result
    }

}

class ConnectionChoiceDialog : Dialog<Connection>(), AppDialog<Connection?> {
    init {
        val grid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            maxWidth = Double.MAX_VALUE
            alignment = Pos.CENTER_LEFT
        }

        title = translate("dialog.connection")
        dialogPane.headerText = translate("dialog.connection")
        dialogPane.maxWidth = Double.MAX_VALUE
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        val connectionTypeBox = ComboBox<ConnectionType>().apply {
            items.addAll(Connections.getTypes())
            selectionModel.selectFirst()
            converter = ConnectionTypeStringConverter()
            maxWidth = Double.MAX_VALUE
        }

        val connectionTypeLabel = Label(translate("dialog.connection.type"))
        val connectionLabel = Label(translate("dialog.connection.connection"))
        val connectionBox = ComboBox<Connection>().apply {
            converter = ConnectionStringConverter()
        }

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.isDisable = true

        val testButton = Button(translate("dialog.connection.test_connection"))
        testButton.isDisable = true

        val infoLabel = Label("")

        val progressBar = ProgressBar().apply {
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
        }
        progressBar.isVisible = false

        testButton.setOnAction {
            val testTask = TestConnectionTask(connectionBox.selectionModel.selectedItem)
            testTask.stateProperty().addListener { _, _, newValue ->
                if (newValue == Worker.State.SUCCEEDED && testTask.value == true) {
                    okButton.isDisable = false
                    progressBar.isVisible = false
                    infoLabel.text = translate("dialog.connection.testing_success")
                } else if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED || (newValue == Worker.State.SUCCEEDED && testTask.value == false)) {
                    Alert(Alert.AlertType.ERROR, translate("dialog.connection.testing_error")).apply {
                        initOwner(WindowContext.get().primaryStage)
                    }.showAndWait()
                    infoLabel.text = translate("dialog.connection.testing_error")
                    okButton.isDisable = true
                    progressBar.isVisible = false
                }
            }
            Thread(testTask).start()
            infoLabel.text = translate("dialog.connection.testing_connection")
            progressBar.isVisible = true
        }

        connectionBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            okButton.isDisable = true
            testButton.isDisable = newValue == null
            dialogPane.scene.window.sizeToScene()
        }

        connectionTypeBox.selectionModel.selectedItemProperty().addListener { _, _, newType ->
            val listTask = ListConnectionsTask(newType)
            listTask.stateProperty().addListener { _, _, newValue ->
                if (newValue == Worker.State.SUCCEEDED) {
                    testButton.isDisable = false
                    progressBar.isVisible = false
                    connectionBox.items.clear()
                    val connections = listTask.value
                    connectionBox.items.addAll(connections)
                    if (connections.isNotEmpty()) {
                        testButton.isDisable = false
                        infoLabel.text = translate("dialog.connection.connections_found", connections.size)
                        connectionBox.selectionModel.selectFirst()
                    } else {
                        infoLabel.text = ""
                        okButton.isDisable = true
                        testButton.isDisable = true
                    }
                } else if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED) {
                    Alert(Alert.AlertType.ERROR, translate("dialog.connection.loading_error")).apply { initOwner(WindowContext.get().primaryStage) }
                        .showAndWait()
                    close()
                }
            }
            Thread(listTask).start()
            progressBar.isVisible = true
            infoLabel.text = translate("dialog.connection.scanning")
        }

        grid.add(connectionTypeBox, 1, 1)
        grid.add(connectionTypeLabel, 0, 1)
        grid.add(connectionBox, 1, 2)
        grid.add(connectionLabel, 0, 2)
        grid.add(testButton, 0, 3)
        grid.add(infoLabel, 0, 4, 2, 1)
        grid.add(progressBar, 0, 5, 2, 1)

        dialogPane.content = grid
        initOwner(WindowContext.get().primaryStage)

        setResultConverter {
            if (it.buttonData == ButtonBar.ButtonData.OK_DONE) {
                ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_CONNECTION] =
                    "${connectionTypeBox.selectionModel.selectedItem.simpleName}:${connectionBox.selectionModel.selectedItem.simpleName}"
                connectionBox.selectionModel.selectedItem
            } else {
                null
            }
        }
    }

    override fun showDialog(): Connection? {
        val result = showAndWait()
        return if (result.isPresent) result.get() else null
    }
}
