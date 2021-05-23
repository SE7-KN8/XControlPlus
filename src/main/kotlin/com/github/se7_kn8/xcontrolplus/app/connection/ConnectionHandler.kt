package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.app.dialog.ConnectionChoiceDialog
import com.github.se7_kn8.xcontrolplus.protocol.Connection
import javafx.beans.property.SimpleObjectProperty

class ConnectionHandler {

    val connection = SimpleObjectProperty<Connection?>()

    fun showConnectionSelectDialog() {
        connection.value = ConnectionChoiceDialog().showDialog()
    }

    fun hasConnection() = connection.get() != null
}
