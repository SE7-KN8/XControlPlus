package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
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
    override fun toString(type: ConnectionType?) = type?.name

    override fun fromString(string: String?) = throw NotImplementedError("Not necessary")
}

class ConnectionStringConverter : StringConverter<Connection>() {
    override fun toString(conn: Connection?) = conn?.name

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

        title = "Connect..."
        dialogPane.headerText = "Choose a connection"
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        val connectionTypeBox = ComboBox<ConnectionType>().apply {
            items.addAll(Connections.getTypes())
            selectionModel.selectFirst()
            converter = ConnectionTypeStringConverter()
        }


        val connectionTypeLabel = Label("Type:")

        val connectionLabel = Label("Connection:")
        val connectionBox = ComboBox<Connection>().apply {
            converter = ConnectionStringConverter()
        }

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.isDisable = true

        val testButton = Button("Test connection")
        testButton.isDisable = true

        val infoLabel = Label("")

        val progressBar = ProgressBar().apply {
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
        }
        progressBar.isVisible = false

        testButton.setOnAction {
            val task = TestConnectionTask(connectionBox.selectionModel.selectedItem)
            task.stateProperty().addListener { _, _, newValue ->
                if (newValue == Worker.State.SUCCEEDED && task.value == true) {
                    okButton.isDisable = false
                    progressBar.isVisible = false
                    infoLabel.text = "Successfully tested"
                } else if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED || (newValue == Worker.State.SUCCEEDED && task.value == false)) {
                    Alert(Alert.AlertType.ERROR, "Error while testing connection").showAndWait()
                    infoLabel.text = "Error while testing connection"
                    okButton.isDisable = true
                    progressBar.isVisible = false
                }
            }
            Thread(task).start()
            infoLabel.text = "Testing connection"
            progressBar.isVisible = true
        }

        connectionBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            okButton.isDisable = true
            testButton.isDisable = newValue == null
        }

        connectionTypeBox.selectionModel.selectedItemProperty().addListener { _, _, newType ->
            val task = ListConnectionsTask(newType)
            task.stateProperty().addListener { _, _, newValue ->
                if (newValue == Worker.State.SUCCEEDED) {
                    testButton.isDisable = false
                    progressBar.isVisible = false
                    connectionBox.items.clear()
                    val connections = task.value
                    connectionBox.items.addAll(connections)
                    if (connections.isNotEmpty()) {
                        testButton.isDisable = false
                        infoLabel.text = "Found ${connections.size} possible connection\n"
                        connectionBox.selectionModel.selectFirst()
                    } else {
                        infoLabel.text = ""
                        okButton.isDisable = true
                        testButton.isDisable = true
                    }
                } else if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED) {
                    Alert(Alert.AlertType.ERROR, "Error while loading connections").showAndWait()
                    close()
                }
            }
            Thread(task).start()
            progressBar.isVisible = true
            infoLabel.text = "Scanning for connections"
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
