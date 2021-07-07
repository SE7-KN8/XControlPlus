package com.github.se7_kn8.xcontrolplus

import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.github.se7_kn8.xcontrolplus.app.util.trace
import javafx.application.Preloader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

class Preloader : Preloader() {

    private lateinit var stage: Stage

    override fun start(splashStage: Stage) {

        val root = StackPane()
        val imageView = ImageView(FileUtil.getImage("logo/splash.png"))
        root.children.add(imageView)

        stage = splashStage
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.scene = Scene(root, 600.0, 330.0)
        imageView.fitWidthProperty().bind(stage.scene.widthProperty())
        imageView.fitHeightProperty().bind(stage.scene.heightProperty())

        stage.icons.addAll(
            Image(javaClass.getResourceAsStream("/assets/logo/large.png")),
            Image(javaClass.getResourceAsStream("/assets/logo/medium.png")),
            Image(javaClass.getResourceAsStream("/assets/logo/small.png"))
        )

        stage.scene.fill = Color.TRANSPARENT
        stage.scene.stylesheets.add(FileUtil.getAsset("style/transparent.css").toExternalForm())
        stage.show()
    }


    override fun handleStateChangeNotification(info: StateChangeNotification) {
        trace { "New state: ${info.type}" }
        if (info.type == StateChangeNotification.Type.BEFORE_START) {
            stage.hide()
        }
    }

}
