package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import javafx.scene.control.TextInputDialog

class TextInputDialog(header: String, defaultValue: String = "") : TextInputDialog(defaultValue), AppDialog<String?> {

    init {
        initOwner(WindowContext.get().primaryStage)
        headerText = header
    }

    override fun showDialog(): String? {
        val result = this.showAndWait()
        if (result.isPresent && result.get().isNotBlank()) {
            return result.get()
        }
        return null
    }
}
