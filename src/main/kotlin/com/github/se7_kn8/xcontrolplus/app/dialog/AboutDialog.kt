package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import java.util.*

class AboutDialog : Alert(AlertType.INFORMATION, "", ButtonType.OK), AppDialog<Unit> {

    init {
        initOwner(WindowContext.get().primaryStage)
        title = translate("dialog.about")
        headerText = translate("dialog.about.header")
        val buildInfo = Properties()
        buildInfo.load(FileUtil.getResourceAsStream("build.properties"))
        val stringBuilder = StringBuilder()

        stringBuilder.append(translate("dialog.about.content"))
        stringBuilder.append("\n\n\n")
        stringBuilder.append(translate("dialog.about.build_info"))
        stringBuilder.append("\n")
        for (key in buildInfo.keys()) {
            stringBuilder.append("$key: ${buildInfo[key]}\n")
        }
        contentText = stringBuilder.toString()
    }

    override fun showDialog() {
        showAndWait()
    }

}
