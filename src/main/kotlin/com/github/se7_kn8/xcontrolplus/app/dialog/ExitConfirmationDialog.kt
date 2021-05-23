package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

class ExitConfirmationDialog : Alert(AlertType.CONFIRMATION, "Do you really want to exit?", ButtonType.OK, ButtonType.CANCEL), AppDialog<Boolean> {

    init {
        initOwner(WindowContext.get().primaryStage)
    }

    override fun showDialog(): Boolean {
        val result = showAndWait()
        if (result.isPresent && result.get() == ButtonType.OK) {
            return true
        }
        return false
    }
}
