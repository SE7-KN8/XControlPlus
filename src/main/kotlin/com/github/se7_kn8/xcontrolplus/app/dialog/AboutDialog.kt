package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.Region

class AboutDialog : Alert(AlertType.INFORMATION, "", ButtonType.OK), AppDialog<Unit> {

    init {
        initOwner(WindowContext.get().primaryStage)
        title = translate("dialog.about")
        headerText = translate("dialog.about.header")
        val stringBuilder = StringBuilder()

        stringBuilder.append(translate("dialog.about.content"))
        stringBuilder.append("\n\n\n")
        stringBuilder.append(translate("dialog.about.build_info"))
        stringBuilder.append("\n")
        for (key in ApplicationContext.get().buildInfo.properties.keys()) {
            stringBuilder.append("$key: ${ApplicationContext.get().buildInfo.properties[key]}\n")
        }

        stringBuilder.append("\n")
        stringBuilder.append(translate("dialog.about.runtime_info"))
        stringBuilder.append("\n")
        stringBuilder.append("Java-Version: ${System.getProperty("java.runtime.version")}\n")
        stringBuilder.append("JavaFX-Version: ${System.getProperty("javafx.runtime.version")}\n")
        stringBuilder.append("OS: ${System.getProperty("os.name")}\n")
        stringBuilder.append("Arch: ${System.getProperty("os.arch")}\n")

        isResizable = true
        dialogPane.minHeight = Region.USE_PREF_SIZE
        dialogPane.maxHeight = Double.MAX_VALUE
        contentText = stringBuilder.toString()
        dialogPane.scene.window.sizeToScene()
    }

    override fun showDialog() {
        showAndWait()
    }

}
