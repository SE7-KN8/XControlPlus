package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.dialog.ExitConfirmationDialog
import com.github.se7_kn8.xcontrolplus.app.dialog.SettingsDialog
import com.github.se7_kn8.xcontrolplus.app.dialog.TextInputDialog
import com.github.se7_kn8.xcontrolplus.app.grid.GridHelper
import com.github.se7_kn8.xcontrolplus.app.grid.GridShortcuts
import com.github.se7_kn8.xcontrolplus.app.project.ProjectManager
import com.github.se7_kn8.xcontrolplus.app.project.Sheet
import com.github.se7_kn8.xcontrolplus.app.project.SheetTab
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import com.github.se7_kn8.xcontrolplus.app.settings.UserSettings
import com.github.se7_kn8.xcontrolplus.app.toolbox.Tool
import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolRenderer
import com.github.se7_kn8.xcontrolplus.app.util.debug
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

class XControlPlus : Application() {

    private lateinit var scene: Scene
    private val projectManager = ProjectManager()
    private val currentTab = SimpleObjectProperty<SheetTab>()
    private val currentGridHelper = SimpleObjectProperty<GridHelper>()

    override fun init() {
        debug("Init application")
        ApplicationContext.init()
        ApplicationContext.get().applicationSettings.load()
        ApplicationContext.get().userSettings.load()
        debug("End of init")
    }

    override fun start(stage: Stage) {
        debug("Start application")
        WindowContext.init(stage)

        val toolRenderer = ToolRenderer()
        val shortcuts = GridShortcuts(toolRenderer)

        currentTab.addListener { _, _, newValue ->
            debug("Current tab has changed to: $newValue")
            newValue?.let {
                currentGridHelper.set(it.sheet.gridHelper)
            }
        }

        currentGridHelper.addListener { _, oldValue, newValue ->
            debug("Current grid helper has changed to $newValue")
            oldValue?.let {
                debug("Detach from old grid helper ($oldValue)")
                toolRenderer.detach(oldValue)
                shortcuts.detach(oldValue)
            }
            toolRenderer.attach(newValue)
            shortcuts.attach(newValue)
        }

        val left = getLeftNode()
        val right = getRightNode(toolRenderer)
        val top = getTopNode()
        val bottom = getBottomNode()

        // Setup root node
        val createProjectLabel = Label(translate("label.new_project_hint"))
        val root = BorderPane()
        root.left = left
        root.right = right
        root.top = top
        root.bottom = bottom
        root.center = createProjectLabel

        root.right.isVisible = false
        root.bottom.isVisible = false
        root.left.isVisible = false

        val addMoreTab = Tab(translate("tab.add")).apply {
            isClosable = false
            tooltip = Tooltip(translate("tooltip.add_sheet"))
        }

        projectManager.activeProject.addListener { _, _, newProject ->
            if (newProject == null) { // No active project
                debug("Active project has been closed")
                root.center = createProjectLabel
                // Disable toolbar
                root.right.isVisible = false
                root.left.isVisible = false
                root.bottom.isVisible = false
                stage.title = translate("stage.title")
            } else {  // A project has been loaded / created
                debug("New project")
                val projectRoot = TabPane()
                projectRoot.isFocusTraversable = true
                val sheetToTab = HashMap<Sheet, SheetTab>()
                projectRoot.tabs.add(addMoreTab)
                root.center = projectRoot
                // Enable toolbar
                root.right.isVisible = true
                root.left.isVisible = true
                root.bottom.isVisible = true
                stage.title = "${translate("stage.title")} - ${newProject.name}"

                projectRoot.selectionModel.selectedItemProperty().addListener { _, oldTab, newTab ->
                    if (newTab is SheetTab) {
                        currentTab.set(newTab)
                    }
                    if (newTab == addMoreTab) {
                        if (projectRoot.tabs.size == 1) {
                            // Only this tab exist anymore, so the project can be closed
                            projectManager.closeProject()
                        } else {
                            // Select first the old tab, so there is a selection even when the dialog is closed
                            projectRoot.selectionModel.select(oldTab)
                            TextInputDialog(translate("dialog.sheet_name")).showDialog()?.let {
                                // Create a new sheet with the name from the dialog
                                newProject.newSheet(it)
                            }
                        }
                    }
                }

                val addSheet: (Sheet) -> Unit = {
                    val tab = SheetTab(newProject, it)
                    projectRoot.tabs.add(projectRoot.tabs.size - 1, tab)
                    projectRoot.selectionModel.select(tab)
                    sheetToTab[it] = tab
                }

                // Add tab for each sheet that already exists
                newProject.sheets.forEach(addSheet)

                newProject.sheets.addListener { change: ListChangeListener.Change<out Sheet> ->
                    while (change.next()) {
                        change.removed.forEach {
                            // Remove tab when sheet is delete
                            projectRoot.tabs.remove(sheetToTab[it])
                            sheetToTab.remove(it)
                            ApplicationContext.get().connectionHandler.removeSheet(it)
                        }
                        change.addedSubList.forEach(addSheet)
                    }
                }
            }
        }

        // Set scene and min size
        scene = Scene(root, 800.0, 600.0)
        stage.scene = scene
        stage.minWidth = 800.0
        stage.minHeight = 600.0

        // Save latest window position and size
        with(ApplicationContext.get().applicationSettings)
        {
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
            Image(javaClass.getResourceAsStream("/assets/logo/large.png")),
            Image(javaClass.getResourceAsStream("/assets/logo/medium.png")),
            Image(javaClass.getResourceAsStream("/assets/logo/small.png"))
        )

        // Show the window
        stage.title = translate("stage.title")

        // TODO to init(), but currently not possible because the property listeners are added in start()
        if (ApplicationContext.get().userSettings[UserSettings.OPEN_LATEST_PROJECT]) {
            projectManager.loadLatestProject()
        }

        stage.show()
        debug("End of start")
    }

    override fun stop() {
        debug { "Stop application" }
        ApplicationContext.get().connectionHandler.closeConnection()
        ApplicationContext.get().applicationSettings.save()
        ApplicationContext.get().userSettings.save()
        debug("End of stop")
    }

    private fun getTopNode(): Node {
        // Setup top node, which is the menu bar
        val top = VBox()
        val fileMenu = Menu(translate("menu.file"))
        val menuBar = MenuBar(fileMenu)
        top.children.addAll(menuBar)

        // Setup menu items
        fileMenu.items.addAll(
            MenuItem(translate("menu.file.new_project")).apply { setOnAction { projectManager.newProject() } },
            MenuItem(translate("menu.file.load_project")).apply { setOnAction { projectManager.loadProject() } },
            MenuItem(translate("menu.file.save_project")).apply { setOnAction { projectManager.saveProject() } },
            MenuItem(translate("menu.file.close_project")).apply { setOnAction { projectManager.closeProject() } },
            SeparatorMenuItem(),
            MenuItem(translate("menu.file.settings")).apply { setOnAction { SettingsDialog().showDialog() } },
            SeparatorMenuItem(),
            MenuItem(translate("menu.file.exit")).apply {
                setOnAction {
                    if (ExitConfirmationDialog().showDialog()) {
                        WindowContext.get().primaryStage.close()
                    }
                }
            }
        )
        return top
    }

    private fun getBottomNode(): Node {
        val bottom = HBox()
        bottom.spacing = 10.0
        bottom.alignment = Pos.CENTER_RIGHT

        val zoomSlider = Slider(0.1, 5.0, 1.0).apply {
            blockIncrement = 0.1
            isSnapToTicks = true
            isShowTickMarks = true
            minorTickCount = 0
            majorTickUnit = 0.2
        }

        // Reset zoom on right click
        zoomSlider.setOnMouseClicked {
            if (it.button == MouseButton.SECONDARY) {
                zoomSlider.value = 1.0
            }
        }


        val mousePosInfo = Label()
        val showGrid = CheckBox()

        val connectionInfo = Label()
        ApplicationContext.get().connectionHandler.connection.addListener { _, _, newValue ->
            if (newValue != null) {
                connectionInfo.text = translate("label.connected_to", newValue.name)
            }
        }

        currentTab.addListener { _, oldValue, newValue ->
            with(newValue.sheet.gridHelper.gridView) {
                zoomSlider.min = minScale
                zoomSlider.max = maxScale

                if (oldValue != null) {
                    oldValue.sheet.gridHelper.gridView.scaleProperty().unbindBidirectional(zoomSlider.valueProperty())
                    oldValue.sheet.gridHelper.gridView.renderGridProperty().unbindBidirectional(showGrid.selectedProperty())
                }

                if (newValue != null) {
                    zoomSlider.value = scale
                    scaleProperty().bindBidirectional(zoomSlider.valueProperty())

                    showGrid.isSelected = isRenderGrid
                    renderGridProperty().bindBidirectional(showGrid.selectedProperty())
                }

                mousePosInfo.textProperty()
                    .bind(Bindings.concat(translate("label.x"), mouseGridXProperty(), translate("label.y"), mouseGridYProperty()))
            }
        }

        bottom.children.addAll(connectionInfo, showGrid, mousePosInfo, zoomSlider)
        return bottom
    }

    private fun getLeftNode(): Node {
        val left = VBox()
        val chooseConnection = Button(translate("button.choose_connection"))

        chooseConnection.setOnAction {
            ApplicationContext.get().connectionHandler.showConnectionSelectDialog()
        }

        left.children.addAll(chooseConnection)
        return left
    }

    private fun getRightNode(toolRenderer: ToolRenderer): Node {
        // Setup right node
        val right = VBox()

        // Only one tool should be selected at the same time
        val toolboxButtonGroup = ToggleGroup()
        toolboxButtonGroup.selectedToggleProperty().addListener { _, oldValue, newValue ->
            if (newValue == null) {
                toolboxButtonGroup.selectToggle(oldValue)
            }
        }
        val buttons = HashMap<Tool, Toggle>()
        toolRenderer.currentTool.addListener { _, _, newValue ->
            scene.cursor = newValue.cursor
            toolboxButtonGroup.selectToggle(buttons[newValue])
        }


        // Add button for each tool
        for (mode in Tool.values()) {
            val button = ToggleButton("", ImageView(mode.getImage()).apply {
                fitWidth = 25.0
                fitHeight = 25.0
            })
            buttons[mode] = button
            button.tooltip = Tooltip(translate("tooltip.${mode.name.lowercase()}"))
            button.setOnMouseClicked {
                toolRenderer.currentTool.set(mode)
            }

            // Select mouse by default
            if (mode == Tool.MOUSE) {
                Platform.runLater {
                    toolboxButtonGroup.selectToggle(button)
                }
            }
            button.toggleGroup = toolboxButtonGroup
            right.children.add(button)
        }
        return right
    }

}
