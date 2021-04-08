package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridState
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolRenderer
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolboxMode
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import com.github.se7_kn8.xcontrolplus.protocol.Connection
import com.github.se7_kn8.xcontrolplus.protocol.Connections
import com.github.se7_kn8.xcontrolplus.protocol.ConnectionType
import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.util.StringConverter

class XControlPlus : Application() {

    private lateinit var scene: Scene
    private var connection: Connection? = null

    override fun start(stage: Stage) {
        val gridView = GridView<BaseCell>()
        val gridState = GridState(gridView)
        val toolRenderer = ToolRenderer(gridView, gridState)

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

        val bottom = HBox()
        bottom.children.add(showGrid)
        bottom.children.add(mousePosInfo)
        bottom.children.add(zoomSlider)
        bottom.spacing = 10.0
        bottom.alignment = Pos.CENTER_RIGHT

        val left = VBox()
        left.children.addAll(
            Button("Save").apply { setOnMouseClicked { gridState.saveToFile(stage) } },
            Button("Load").apply { setOnMouseClicked { gridState.loadFromFile(stage) } })

        val connectionTypeBox = ComboBox<ConnectionType>()
        val typeName = Label()
        val connName = Label()
        val connectionBox = ComboBox<Connection>()
        val testConn = Button("Test conn")
        connectionBox.isVisible = false
        connName.visibleProperty().bind(connectionBox.visibleProperty())
        testConn.visibleProperty().bind(connectionBox.visibleProperty())
        connectionTypeBox.items.addAll(Connections.getTypes())
        connectionTypeBox.converter = object : StringConverter<ConnectionType>() {
            override fun toString(`object`: ConnectionType?): String? {
                return `object`?.name
            }

            override fun fromString(string: String?): ConnectionType {
                throw NotImplementedError("Should be not necessary")
            }

        }
        connectionBox.converter = object : StringConverter<Connection>() {
            override fun toString(`object`: Connection?): String? {
                return `object`?.name
            }

            override fun fromString(string: String?): Connection {
                throw NotImplementedError("Should be not necessary")
            }

        }
        connectionTypeBox.selectionModel.selectFirst()
        connectionTypeBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            typeName.text = newValue.name
            val connections = newValue.listConnections()
            connectionBox.isVisible = connections.size > 0
            connectionBox.items.clear()
            connectionBox.items.addAll(connections)
            connectionBox.selectionModel.selectFirst()
        }

        connectionBox.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
            oldValue?.closeConnection()
            newValue?.openConnection()
            connection = newValue
            newValue?.setOnPacketReceived {
                println("Received packet: $it")
            }
            connName.text = newValue?.name
        }

        testConn.setOnAction {
            connectionBox.selectionModel.selectedItem.openConnection()
            val bool = connectionBox.selectionModel.selectedItem.testConnection(5000)
            println("Success: $bool")
        }

        left.children.addAll(connectionTypeBox, typeName, connectionBox, connName, testConn)

        val toolboxButtonGroup = ToggleGroup()

        val right = VBox()

        for (mode in ToolboxMode.values()) {
            val button = ToggleButton(mode.name)
            button.setOnMouseClicked {
                toolRenderer.currentTool.set(mode)
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
            gridView.widthProperty().bind(widthProperty())
            gridView.heightProperty().bind(heightProperty())
            children.add(gridView)
        }

        scene = Scene(root, 800.0, 600.0)

        scene.setOnKeyTyped {
            when (it.character.toUpperCase()) {
                "R" -> {
                    if (it.isShiftDown) {
                        toolRenderer.rotation = toolRenderer.rotation.rotateCCW()
                    } else {
                        toolRenderer.rotation = toolRenderer.rotation.rotateCW()
                    }
                }
                else -> {
                    println("Unknown key typed: ${it.character}")
                }
            }
        }

        gridView.pauseProperty().bind(stage.iconifiedProperty())
        stage.scene = scene
        stage.minWidth = 1280.0
        stage.minHeight = 720.0
        stage.show()
    }

    override fun stop() {
        super.stop()
        connection?.closeConnection()
    }

}
