package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.grid.toolbox.ToolboxMode
import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.input.KeyCode
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

    private val mouseXProperty = SimpleIntegerProperty(1)
    private val mouseYProperty = SimpleIntegerProperty(1)

    private lateinit var canvas: Canvas
    private lateinit var gridRenderer: GridRenderer
    private lateinit var scene: Scene

    override fun start(stage: Stage) {

        canvas = Canvas(0.0, 0.0)
        gridRenderer = GridRenderer(canvas)
        gridRenderer.start()

        val zoomSlider = Slider(0.1, 5.0, 1.0)
        zoomSlider.blockIncrement = 0.1
        zoomSlider.isSnapToTicks = true
        zoomSlider.isShowTickMarks = true
        zoomSlider.minorTickCount = 0
        zoomSlider.majorTickUnit = 0.2
        zoomSlider.valueProperty().bindBidirectional(gridRenderer.zoomProperty)
        zoomSlider.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                zoomSlider.value = 1.0
            }
        }


        gridRenderer.mouseXProperty.bind(mouseXProperty)
        gridRenderer.mouseYProperty.bind(mouseYProperty)

        val mousePosInfo = Label()
        mousePosInfo.textProperty().bind(Bindings.concat("X: ", mouseXProperty, "Y: ", mouseYProperty))

        val fpsInfo = Label()
        fpsInfo.textProperty().bind(Bindings.concat("FPS: ", gridRenderer.lastFpsProperty))


        val bottom = HBox()
        bottom.children.add(fpsInfo)
        bottom.children.add(mousePosInfo)
        bottom.children.add(zoomSlider)
        bottom.alignment = Pos.CENTER_RIGHT

        val left = VBox()
        //left.children.addAll(Button("Test1"), Button("Test2"), Button("Test3"))

        val toolboxButtonGroup = ToggleGroup()

        val right = VBox()

        for (mode in ToolboxMode.values()) {
            val button = ToggleButton(mode.name)
            button.setOnMouseClicked {
                gridRenderer.toolboxMode = mode
                scene.cursor = mode.getCursor()

            }
            button.toggleGroup = toolboxButtonGroup
            right.children.add(button)
        }

        val top = VBox()
        top.children.addAll(MenuBar(), ToolBar())


        val root = BorderPane()
        root.left = left
        root.top = top
        root.bottom = bottom
        root.right = right
        root.center = Pane().apply {
            setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE)
            canvas.widthProperty().bind(widthProperty())
            canvas.heightProperty().bind(heightProperty())
            children.add(canvas)
        }

        scene = Scene(root, 800.0, 600.0)
        scene.setOnScroll {
            val zoom = it.deltaY * it.multiplierY * 0.0001 + 1.0
            var newValue: Double = gridRenderer.zoomProperty.get() * zoom
            if (!it.isAltDown) {
                newValue = min(newValue, zoomSlider.max)
                newValue = max(newValue, zoomSlider.min)
            }
            gridRenderer.zoomProperty.set(newValue)
            updateMousePos(it.x, it.y)
        }

        scene.setOnMouseMoved {
            updateMousePos(it.x, it.y)
        }
        scene.setOnMousePressed {
            moveOffsetX = gridRenderer.zoomCenterX * gridRenderer.zoomProperty.get()
            moveOffsetY = gridRenderer.zoomCenterY * gridRenderer.zoomProperty.get()
            mouseStartPosX = it.x
            mouseStartPosY = it.y
            if (it.button == MouseButton.PRIMARY) {
                gridRenderer.onClick()
            }
        }

        scene.setOnMouseDragged {
            if (it.button == MouseButton.MIDDLE) {
                val deltaX = it.x - mouseStartPosX
                val deltaY = it.y - mouseStartPosY
                gridRenderer.zoomCenterX = (moveOffsetX - deltaX) / gridRenderer.zoomProperty.get()
                gridRenderer.zoomCenterY = (moveOffsetY - deltaY) / gridRenderer.zoomProperty.get()
            }
        }

        scene.setOnKeyTyped {
            when (it.character.toUpperCase()) {
                "R" -> {
                    gridRenderer.rotation = gridRenderer.rotation.next()
                }
                else -> {
                    println("Unknown key typed: ${it.character}")
                }
            }
        }

        stage.scene = scene
        stage.minWidth = 1280.0
        stage.minHeight = 720.0
        stage.show()
    }

    private fun updateMousePos(screenX: Double, screenY: Double) {
        var tX = gridRenderer.transformScreenX(screenX - canvas.parent.layoutX) / GRID_SIZE
        var tY = gridRenderer.transformScreenY(screenY - canvas.parent.layoutY) / GRID_SIZE
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
        mouseXProperty.set(tX.toInt())
        mouseYProperty.set(tY.toInt())
    }

}


