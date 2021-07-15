package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.dialog.ConfirmationDialog
import com.github.se7_kn8.xcontrolplus.app.dialog.TextInputDialog
import com.github.se7_kn8.xcontrolplus.app.grid.GridHelper
import com.github.se7_kn8.xcontrolplus.app.settings.UserSettings
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane

class LongPressTimer(private val gridHelper: GridHelper) : AnimationTimer() {

    private var lastStart: Long = 0

    override fun handle(now: Long) {
        if ((now - lastStart) > 400000000) {
            gridHelper.getHoveredCell().ifPresent {
                it.getContextOptions().firstOrNull()?.onAction?.handle(ActionEvent())
            }
            stop()
        }
    }

    override fun start() {
        super.start()
        lastStart = System.nanoTime()
    }
}

class SheetTab(project: Project, val sheet: Sheet) : Tab(sheet.name.get()) {

    init {
        // This should only be happen in javafx application thread because we start rendering
        if (!Platform.isFxApplicationThread()) {
            throw IllegalStateException()
        }
        val longPressTimer = LongPressTimer(sheet.gridHelper)
        with(sheet.gridHelper.gridView) {
            addEventFilter(MouseEvent.ANY) {
                if (ApplicationContext.get().userSettings[UserSettings.SINGLE_TOUCH_MODE]) {
                    this.moveMouseButton = MouseButton.PRIMARY
                    when (it.eventType) {
                        MouseEvent.MOUSE_PRESSED -> {
                            longPressTimer.start()
                        }
                        MouseEvent.MOUSE_DRAGGED -> {
                        }
                        else -> {
                            longPressTimer.stop()
                        }
                    }
                } else if (ApplicationContext.get().userSettings[UserSettings.SINGLE_TOUCH_TO_TURN] && it.eventType == MouseEvent.MOUSE_PRESSED) {
                    sheet.gridHelper.getHoveredCell().ifPresent { cell ->
                        cell.getContextOptions().firstOrNull()?.onAction?.handle(ActionEvent())
                    }
                } else {
                    this.moveMouseButton = MouseButton.MIDDLE
                }
            }
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

