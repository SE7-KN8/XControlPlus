package com.github.se7_kn8.xcontrolplus

import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.github.se7_kn8.xcontrolplus.app.util.trace
import javafx.application.Preloader
import javafx.scene.Scene
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class Preloader : Preloader() {

    private lateinit var stage: Stage

    override fun start(splashStage: Stage) {

        val root = StackPane()
        root.children.add(ImageView(FileUtil.getImage("logo/splash.png")))

        stage = splashStage
        stage.scene = Scene(root, 768.0, 357.0)
        stage.show()
    }


    override fun handleStateChangeNotification(info: StateChangeNotification) {
        trace { "New state: ${info.type}" }
        if (info.type == StateChangeNotification.Type.BEFORE_START) {
            stage.hide()
        }
    }

}
