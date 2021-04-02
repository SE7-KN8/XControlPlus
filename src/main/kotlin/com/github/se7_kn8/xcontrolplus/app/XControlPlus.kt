package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.grid.GRID_SIZE
import com.github.se7_kn8.xcontrolplus.app.grid.GridRenderer
import com.github.se7_kn8.xcontrolplus.app.grid.GridState
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolboxMode
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
    private val gridState = GridState()
    private lateinit var canvas: Canvas
    private lateinit var gridRenderer: GridRenderer
    private lateinit var scene: Scene

    override fun start(stage: Stage) {

        canvas = Canvas(0.0, 0.0)
        gridRenderer = GridRenderer(canvas, gridState)
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

        val mousePosInfo = Label()
        mousePosInfo.textProperty().bind(Bindings.concat("X:", gridRenderer.mouseGridXProperty, " Y: ", gridRenderer.mouseGridYProperty))

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
        left.children.addAll(
            Button("Save").apply { setOnMouseClicked { gridState.saveToFile(stage) } },
            Button("Load").apply { setOnMouseClicked { gridState.loadFromFile(stage) } })

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

}
