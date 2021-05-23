package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.connection.ConnectionHandler
import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.dialog.ExitConfirmationDialog
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridShortcuts
import com.github.se7_kn8.xcontrolplus.app.grid.GridState
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import com.github.se7_kn8.xcontrolplus.app.settings.UserSettings
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolRenderer
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolboxMode
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.Stage

class XControlPlus : Application() {

    private lateinit var scene: Scene
    private val connectionHandler = ConnectionHandler()

    override fun init() {
        ApplicationContext.init()
        ApplicationContext.get().applicationSettings.load()
        ApplicationContext.get().userSettings.load()
    }

    override fun start(stage: Stage) {
        WindowContext.init(stage)
        val gridView = GridView<BaseCell>()
        gridView.pauseProperty().bind(stage.iconifiedProperty())
        val gridState = GridState(gridView)

        val shortcuts = GridShortcuts(gridState)
        val toolRenderer = ToolRenderer(gridState)


        gridView.isHighlightSelectedCell = true

        val left = getLeftNode()
        val right = getRightNode(gridView, toolRenderer)
        val top = getTopNode(gridState)
        val bottom = getBottomNode(gridView)


        // Setup root node
        val root = BorderPane()
        root.left = left
        root.right = right
        root.top = top
        root.bottom = bottom
        root.center = Pane().apply {
            // Set canvas size to largest possible size
            setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE)
            gridView.widthProperty().bind(widthProperty())
            gridView.heightProperty().bind(heightProperty())
            children.add(gridView)
        }


        // Set scene and min size
        scene = Scene(root, 800.0, 600.0)
        stage.scene = scene
        stage.minWidth = 800.0
        stage.minHeight = 600.0

        // Save latest window posistion and size
        with(ApplicationContext.get().applicationSettings) {
            stage.isMaximized = this[ApplicationSettings.START_MAXIMIZED]
            stage.x = this[ApplicationSettings.WINDOW_X].toDouble()
            stage.y = this[ApplicationSettings.WINDOW_Y].toDouble()
            stage.width = this[ApplicationSettings.WINDOW_WIDTH].toDouble()
            stage.height = this[ApplicationSettings.WINDOW_HEIGHT].toDouble()

            stage.maximizedProperty().addListener { _, _, newValue -> this[ApplicationSettings.START_MAXIMIZED] = newValue }
            stage.xProperty().addListener { _, _, newValue ->
                if (!stage.isMaximized) {
                    this[ApplicationSettings.WINDOW_X] = newValue.toDouble()
                }
            }
            stage.yProperty().addListener { _, _, newValue ->
                if (!stage.isMaximized) {
                    this[ApplicationSettings.WINDOW_Y] = newValue.toDouble()
                }
            }
            stage.widthProperty().addListener { _, _, newValue ->
                if (!stage.isMaximized) {
                    this[ApplicationSettings.WINDOW_WIDTH] = newValue.toDouble()
                }
            }
            stage.heightProperty().addListener { _, _, newValue ->
                if (!stage.isMaximized) {
                    this[ApplicationSettings.WINDOW_HEIGHT] = newValue.toDouble()
                }
            }
        }


        // Add "Confirmation before exit" dialog
        stage.setOnCloseRequest { event ->
            if (ApplicationContext.get().userSettings[UserSettings.ASK_BEFORE_EXIT]) {
                if (!ExitConfirmationDialog().showDialog()) {
                    event.consume()
                }
            }
        }


        // Set window icons
        //FIXME Currently not working with intellij because: https://youtrack.jetbrains.com/issue/IDEA-197469
        stage.icons.addAll(
            Image(javaClass.getResourceAsStream("/logo/large.png")),
            Image(javaClass.getResourceAsStream("/logo/medium.png")),
            Image(javaClass.getResourceAsStream("/logo/small.png"))
        )

        // Show the window
        stage.show()
    }

    override fun stop() {
        super.stop()
        connectionHandler.connection.value?.closeConnection()
        ApplicationContext.get().applicationSettings.save()
        ApplicationContext.get().userSettings.save()
    }

    private fun getTopNode(gridState: GridState): Node {
        // Setup top node, which is the menu bar
        val top = VBox()
        val fileMenu = Menu("File")
        val menuBar = MenuBar(fileMenu)
        top.children.addAll(menuBar)

        // Setup menu items
        fileMenu.items.addAll(
            MenuItem("Load Project").apply { setOnAction { gridState.loadFromFile() } },
            MenuItem("Save Project").apply { setOnAction { gridState.saveToFile() } },
            SeparatorMenuItem(),
            MenuItem("Exit").apply {
                setOnAction {
                    if (ExitConfirmationDialog().showDialog()) {
                        WindowContext.get().primaryStage.close()
                    }
                }
            }
        )
        return top
    }

    private fun getBottomNode(gridView: GridView<*>): Node {

        val bottom = HBox()
        bottom.spacing = 10.0
        bottom.alignment = Pos.CENTER_RIGHT

        val zoomSlider = Slider(0.1, 5.0, 1.0).apply {
            blockIncrement = 0.1
            isSnapToTicks = true
            isShowTickMarks = true
            minorTickCount = 0
            majorTickUnit = 0.2
            gridView.minScaleProperty().bind(minProperty())
            gridView.maxScaleProperty().bind(maxProperty())
            gridView.scaleProperty().bindBidirectional(valueProperty())
        }
        // Reset zoom on right click
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

        bottom.children.addAll(connectionInfo, showGrid, mousePosInfo, zoomSlider)
        return bottom
    }

    private fun getLeftNode(): Node {
        val left = VBox()
        val chooseConnection = Button("Choose connection")

        chooseConnection.setOnAction {
            connectionHandler.showConnectionSelectDialog()
        }

        left.children.addAll(chooseConnection)
        return left
    }

    private fun getRightNode(gridView: GridView<*>, toolRenderer: ToolRenderer): Node {
        // Setup right node
        val right = VBox()

        // Only one tool should be selected at the same time
        val toolboxButtonGroup = ToggleGroup()
        toolboxButtonGroup.selectedToggleProperty().addListener { _, oldValue, newValue ->
            if (newValue == null) {
                toolboxButtonGroup.selectToggle(oldValue)
            }
        }


        // Add button for each tool
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
        return right
    }

}
