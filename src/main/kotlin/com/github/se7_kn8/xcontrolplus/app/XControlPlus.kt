package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.grid.toolbox.ToolboxMode
import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Point2D
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

    private val mouseGridPosX = SimpleIntegerProperty(0)
    private val mouseGridPosY = SimpleIntegerProperty(0)

    private var moveOffset = Point2D(0.0, 0.0)
    private var mouseStartPos = Point2D(0.0, 0.0)

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
        gridRenderer.mouseGridXProperty.bind(mouseGridPosX)
        gridRenderer.mouseGridYProperty.bind(mouseGridPosY)

        val mousePosInfo = Label()
        mousePosInfo.textProperty().bind(Bindings.concat("X:", mouseGridPosX, " Y: ", mouseGridPosY))

        val fpsInfo = Label()
        fpsInfo.textProperty().bind(Bindings.concat("FPS: ", gridRenderer.lastFpsProperty))

        val showGrid = CheckBox()
        showGrid.selectedProperty().bindBidirectional(gridRenderer.showGridProperty)

        val bottom = HBox()
        bottom.children.add(showGrid)
        bottom.children.add(fpsInfo)
        bottom.children.add(mousePosInfo)
        bottom.children.add(zoomSlider)
        bottom.spacing = 10.0
        bottom.alignment = Pos.CENTER_RIGHT

        val left = VBox()
        left.children.addAll(Button("Test1"), Button("Test2"), Button("Test3"))

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
            updateMousePos(Point2D(it.x, it.y))
        }

        scene.setOnMouseMoved {
            updateMousePos(Point2D(it.x, it.y))
        }
        scene.setOnMousePressed {
            moveOffset = gridRenderer.translateProperty.get()
            mouseStartPos = Point2D(it.x, it.y)
            if (it.button == MouseButton.PRIMARY) {
                gridRenderer.onClick()
            }
        }

        scene.setOnMouseDragged {
            if (it.button == MouseButton.MIDDLE) {
                gridRenderer.translateProperty.set(Point2D(it.x, it.y).add(moveOffset).subtract(mouseStartPos))
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

    private fun updateMousePos(mousePos: Point2D) {
        val grid = gridRenderer.transformScreen(mousePos.subtract(canvas.parent.layoutX, canvas.parent.layoutY)).multiply(1.0 / GRID_SIZE)
        var x = grid.x
        var y = grid.y

        if (x < 0.0) {
            x -= 1.0
        }
        if (y < 0.0) {
            y -= 1.0
        }

        mouseGridPosX.value = x.toInt()
        mouseGridPosY.value = y.toInt()
    }

}


