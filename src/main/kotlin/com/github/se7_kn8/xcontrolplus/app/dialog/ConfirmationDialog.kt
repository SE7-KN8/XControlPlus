package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

open class ConfirmationDialog(content: String) : Alert(AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO),
    AppDialog<Boolean> {
    init {
        initOwner(WindowContext.get().primaryStage)
    }

    override fun showDialog(): Boolean {
        val result = showAndWait()
        if (result.isPresent && result.get() == ButtonType.YES) {
            return true
        }
        return false
    }
}

class ExitConfirmationDialog : ConfirmationDialog(translate("dialog.exit_confirmation"))
