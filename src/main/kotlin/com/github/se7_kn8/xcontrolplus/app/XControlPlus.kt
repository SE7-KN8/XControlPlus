package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.connection.ConnectionHandler
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridShortcuts
import com.github.se7_kn8.xcontrolplus.app.grid.GridState
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolRenderer
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolboxMode
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.Stage

class XControlPlus : Application() {

    private lateinit var scene: Scene
    private val connectionHandler = ConnectionHandler()

    override fun start(stage: Stage) {
        val gridView = GridView<BaseCell>()
        val gridState = GridState(gridView)

        val shortcuts = GridShortcuts(gridState)
        val toolRenderer = ToolRenderer(gridState)


        gridView.isHighlightSelectedCell = true

        val zoomSlider = Slider(0.1, 5.0, 1.0)
        gridView.minScaleProperty().bind(zoomSlider.minProperty())
        gridView.maxScaleProperty().bind(zoomSlider.maxProperty())
        zoomSlider.blockIncrement = 0.1
        zoomSlider.isSnapToTicks = true
        zoomSlider.isShowTickMarks = true
        zoomSlider.minorTickCount = 0
        zoomSlider.majorTickUnit = 0.2
        zoomSlider.valueProperty().bindBidirectional(gridView.scaleProperty())
        zoomSlider.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                zoomSlider.value = 1.0
            }
        }

        val mousePosInfo = Label()
        mousePosInfo.textProperty().bind(Bindings.concat("X:", gridView.mouseGridXProperty(), " Y: ", gridView.mouseGridYProperty()))

        val showGrid = CheckBox()
        showGrid.selectedProperty().bindBidirectional(gridView.renderGridProperty())

        val connectionInfo = Label()
        connectionHandler.connection.addListener { _, _, newValue ->
            if (newValue != null) {
                connectionInfo.text = "Connected to: " + newValue.name
            }
        }

        val bottom = HBox()
        bottom.children.addAll(connectionInfo, showGrid, mousePosInfo, zoomSlider)
        bottom.spacing = 10.0
        bottom.alignment = Pos.CENTER_RIGHT

        val left = VBox()
        left.children.addAll(
            Button("Save").apply { setOnMouseClicked { gridState.saveToFile(stage) } },
            Button("Load").apply { setOnMouseClicked { gridState.loadFromFile(stage) } })

        val chooseConnection = Button("Choose connection")

        chooseConnection.setOnAction {
            connectionHandler.showConnectionSelectDialog()
        }

        left.children.addAll(chooseConnection)

        val toolboxButtonGroup = ToggleGroup()
        toolboxButtonGroup.selectedToggleProperty().addListener { _, oldValue, newValue ->
            if (newValue == null) {
                toolboxButtonGroup.selectToggle(oldValue)
            }
        }

        val right = VBox()

        for (mode in ToolboxMode.values()) {
            val button = ToggleButton(mode.name)
            button.setOnMouseClicked {
                gridView.isHighlightSelectedCell = mode == ToolboxMode.MOUSE
                gridView.selectedCell = null
                toolRenderer.currentTool.set(mode)
                scene.cursor = mode.getCursor()

            }
            if (mode == ToolboxMode.MOUSE) {
                Platform.runLater {
                    toolboxButtonGroup.selectToggle(button)
                    button.requestFocus()
                }
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
            gridView.widthProperty().bind(widthProperty())
            gridView.heightProperty().bind(heightProperty())
            children.add(gridView)
        }

        scene = Scene(root, 800.0, 600.0)

        gridView.pauseProperty().bind(stage.iconifiedProperty())
        stage.scene = scene
        stage.minWidth = 1280.0
        stage.minHeight = 720.0
        stage.show()
    }

    override fun stop() {
        super.stop()
        connectionHandler.connection.value?.closeConnection()
    }

}
