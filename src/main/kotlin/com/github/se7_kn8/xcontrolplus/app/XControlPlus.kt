package com.github.se7_kn8.xcontrolplus.app

import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.stage.Stage
import kotlin.math.max
import kotlin.math.min

class XControlPlus : Application() {

    private var moveOffsetX = 0.0
    private var moveOffsetY = 0.0
    private var mouseStartPosX = 0.0
    private var mouseStartPosY = 0.0

    override fun start(stage: Stage) {

        val canvas = Canvas(0.0, 0.0)
        val timer = GridRenderer(canvas)
        timer.start()

        val zoomSlider = Slider(0.1, 5.0, 1.0)
        zoomSlider.blockIncrement = 0.1
        zoomSlider.isSnapToTicks = true
        zoomSlider.isShowTickMarks = true
        zoomSlider.minorTickCount = 0
        zoomSlider.majorTickUnit = 0.2
        zoomSlider.valueProperty().bindBidirectional(timer.zoomProperty)
        zoomSlider.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                zoomSlider.value = 1.0
            }
        }

        val mouseXProperty = SimpleDoubleProperty(0.0)
        val mouseYProperty = SimpleDoubleProperty(0.0)
        val mousePosInfo = Label()
        mousePosInfo.textProperty().bind(Bindings.concat("X: ", mouseXProperty, "Y: ", mouseYProperty))


        val bottom = HBox()
        bottom.children.add(mousePosInfo)
        bottom.children.add(zoomSlider)
        bottom.alignment = Pos.CENTER_RIGHT

        val left = VBox()
        left.children.addAll(Button("Test1"), Button("Test2"), Button("Test3"))


        val top = VBox()
        top.children.addAll(MenuBar(), ToolBar())


        val root = BorderPane()
        root.left = left
        root.top = top
        root.bottom = bottom
        root.center = Pane().apply {
            setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE)
            canvas.widthProperty().bind(widthProperty())
            canvas.heightProperty().bind(heightProperty())
            children.add(canvas)
        }

        val scene = Scene(root, 800.0, 600.0)
        scene.setOnScroll {
            val zoom = it.deltaY * it.multiplierY * 0.0001 + 1.0
            var newValue: Double = timer.zoomProperty.get() * zoom
            if (!it.isAltDown) {
                newValue = min(newValue, zoomSlider.max)
                newValue = max(newValue, zoomSlider.min)
            }
            timer.zoomProperty.set(newValue)
        }

        scene.setOnMouseMoved {
            var tX = timer.transformScreenX(it.x - canvas.parent.layoutX) / GRID_SIZE
            var tY = timer.transformScreenY(it.y - canvas.parent.layoutY) / GRID_SIZE
            if (tX > 0.0) {
                tX += 1.0
            } else {
                tX -= 1.0
            }
            if (tY > 0.0) {
                tY += 1.0
            } else {
                tY -= 1.0
            }
            mouseXProperty.set(tX.toInt().toDouble())
            mouseYProperty.set(tY.toInt().toDouble())
        }
        scene.setOnMousePressed {
            moveOffsetX = timer.zoomCenterX * timer.zoomProperty.get()
            moveOffsetY = timer.zoomCenterY * timer.zoomProperty.get()
            mouseStartPosX = it.x
            mouseStartPosY = it.y
            println(
                "Canvas click at: x=${
                    timer.transformScreenX(it.x - canvas.parent.layoutX).toInt()
                } y=${timer.transformScreenY(it.y - canvas.parent.layoutY).toInt()}"
            )
        }
        scene.setOnMouseDragged {
            if (it.button == MouseButton.MIDDLE) {
                val deltaX = it.x - mouseStartPosX
                val deltaY = it.y - mouseStartPosY
                timer.zoomCenterX = (moveOffsetX - deltaX) / timer.zoomProperty.get()
                timer.zoomCenterY = (moveOffsetY - deltaY) / timer.zoomProperty.get()
            }
        }

        stage.scene = scene
        stage.minWidth = 1280.0
        stage.minHeight = 720.0
        stage.show()
    }

}

