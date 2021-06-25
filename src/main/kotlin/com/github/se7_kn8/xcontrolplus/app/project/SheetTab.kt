package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.dialog.ConfirmationDialog
import com.github.se7_kn8.xcontrolplus.app.dialog.TextInputDialog
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.application.Platform
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.layout.Pane

class SheetTab(project: Project, val sheet: Sheet) : Tab(sheet.name.get()) {

    init {
        // This should only be happen in javafx application thread because we start rendering
        if (!Platform.isFxApplicationThread()) {
            throw IllegalStateException()
        }
        with(sheet.gridHelper.gridView) {
            minScale = 0.1
            maxScale = 5.0
            // Only render when visible, to safe resources
            pauseProperty().bind(WindowContext.get().primaryStage.iconifiedProperty())
            textProperty().bind(sheet.name)
            content = Pane().also {
                // Set canvas size to largest possible size
                isFocusTraversable = true
                it.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE)
                widthProperty().bind(it.widthProperty())
                heightProperty().bind(it.heightProperty())
                it.children.add(this)
            }
            // Now rendering can start
            renderer.start()
        }
        setOnCloseRequest { closeEvent ->
            if (ConfirmationDialog(translate("dialog.delete_sheet")).showDialog()) {
                debug("Removing ${sheet.name.value} from project ${project.name}")
                project.sheets.remove(sheet)
            } else {
                closeEvent.consume()
            }
        }

        contextMenu = ContextMenu(
            MenuItem("Rename").apply {
                setOnAction {
                    TextInputDialog("New name?", sheet.name.get()).showDialog()?.let {
                        debug("Renaming sheet ${sheet.name.value} to $it")
                        sheet.name.set(it)
                    }
                }
            }
        )
    }
}

