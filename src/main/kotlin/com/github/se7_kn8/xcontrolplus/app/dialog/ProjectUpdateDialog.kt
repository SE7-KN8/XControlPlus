package com.github.se7_kn8.xcontrolplus.app.dialog

import com.github.se7_kn8.xcontrolplus.app.connection.TurnoutPacket
import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.project.Project
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.concurrent.Task
import javafx.concurrent.Worker
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.VBox

class ProjectUpdateDialog(private val project: Project) : Alert(AlertType.NONE), AppDialog<Unit> {

    class UpdateTurnoutTask(private val project: Project) : Task<Int>() {

        private val sleepTime: Long = 100 // Wait 100ms between updates to give the mcu some process time

        override fun call(): Int {
            val addresses = project.getTurnoutAddresses()
            val connectionHandler = ApplicationContext.get().connectionHandler
            addresses.forEachIndexed { counter, it ->
                if (isCancelled) {
                    return@forEachIndexed
                }
                connectionHandler.sendPacket(TurnoutPacket.newRequest(it))
                Thread.sleep(sleepTime)
                updateValue(counter + 1)
                updateProgress(counter.toLong() + 1, addresses.size.toLong())
            }
            return 0
        }

    }

    private val progressBar = ProgressBar()
    private val infoLabel = Label()

    init {
        initOwner(WindowContext.get().primaryStage)
        title = translate("dialog.project_update")
        headerText = translate("dialog.project_update.turnouts")
        dialogPane.content = VBox().apply { children.addAll(infoLabel, progressBar) }
        dialogPane.minWidth = 500.0
    }

    override fun showDialog() {
        val task = UpdateTurnoutTask(project)
        progressBar.progressProperty().bind(task.progressProperty())
        task.valueProperty().addListener { _, _, newValue -> infoLabel.text = translate("dialog.project_update.updating_turnout", newValue.toInt()) }
        task.stateProperty()
            .addListener { _, _, newValue ->
                if (newValue == Worker.State.SUCCEEDED || newValue == Worker.State.CANCELLED || newValue == Worker.State.FAILED) {
                    dialogPane.buttonTypes.add(ButtonType.CANCEL) // This is necessary because javafx only allows closing a dialog if a cancel button is present
                    close()
                }
            }
        ApplicationContext.get().executor.submit(task)
        showAndWait()
    }
}
