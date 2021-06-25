package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.scene.control.Alert

class NoConnectionDialog : Alert(AlertType.WARNING, translate("dialog.no_connection.hint")), AppDialog<Unit> {

    init {
        initOwner(WindowContext.get().primaryStage)
        headerText = translate("dialog.no_connection")
    }

    override fun showDialog() {
        showAndWait()
    }
}
