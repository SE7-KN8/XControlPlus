package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolboxMode
import com.github.se7_kn8.xcontrolplus.app.util.fillCircle
import com.github.se7_kn8.xcontrolplus.app.util.rotated
import com.github.se7_kn8.xcontrolplus.gridview.CellRotation
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseButton
import javafx.scene.text.Font
import javafx.scene.transform.Affine


const val GRID_SIZE = 32
const val GRID_WIDTH = 100
const val GRID_HEIGHT = 50
const val PIXEL_WIDTH = GRID_SIZE * GRID_WIDTH
const val PIXEL_HEIGHT = GRID_SIZE * GRID_HEIGHT

class GridRenderer(private val canvas: Canvas, private val gridState: GridState) : AnimationTimer() {

    private val gc = canvas.graphicsContext2D

    private var lastTime: Long = 0

    private var fpsAvg = 0.0
    private var fpsCounter = 0

    private var gridTransform = Affine()

    private var firstFrame = true

    private var moveOffset = Point2D(0.0, 0.0)
    private var mouseStartPos = Point2D(0.0, 0.0)

    var toolboxMode = ToolboxMode.MOUSE
    var rotation = CellRotation.D0

    val mouseGridXProperty = SimpleIntegerProperty(0)
    val mouseGridYProperty = SimpleIntegerProperty(0)

    val zoomProperty = SimpleDoubleProperty(1.0)
    val translateProperty = SimpleObjectProperty(Point2D(0.0, 0.0))

    val lastFpsProperty = SimpleIntegerProperty(0)

    val showGridProperty = SimpleBooleanProperty(true)

    init {
        zoomProperty.addListener { _, oldValue, newValue ->
            val scaleFactor = oldValue.toDouble() / newValue.toDouble()
            val middlePoint = transformScreen(Point2D(canvas.width / 2.0, canvas.height / 2.0))
            gridTransform.appendScale(
                scaleFactor,
                scaleFactor,
                middlePoint.x,
                middlePoint.y
            )
            translateProperty.set(Point2D(gridTransform.tx, gridTransform.ty))
        }

        translateProperty.addListener { _, _, newValue ->
            gridTransform.tx = newValue.x
            gridTransform.ty = newValue.y
        }

        canvas.setOnMousePressed {
            moveOffset = translateProperty.get()
            mouseStartPos = Point2D(it.x, it.y)
            if (it.button == MouseButton.PRIMARY) {
                onClick()
            }
        }

        canvas.setOnMouseDragged {
            if (it.button == MouseButton.MIDDLE) {
                val translation = Point2D(it.x, it.y).subtract(mouseStartPos).add(moveOffset)
                translateProperty.set(translation)
            }
        }
        canvas.setOnScroll {
            val zoom = -it.deltaY * it.multiplierY * 0.0001 + 1.0
            val newValue: Double = zoomProperty.get() * zoom
            zoomProperty.set(newValue)
            updateMousePos(Point2D(it.x, it.y))
        }
        canvas.setOnMouseMoved {
            updateMousePos(Point2D(it.x, it.y))
        }
    }


    override fun handle(now: Long) {
        if (firstFrame) {
            firstFrame()
            firstFrame = false
        }
        calcFps()
        clearScreen()
        gc.save()
        gc.transform = gridTransform

        if (showGridProperty.get()) {
            renderGrid()
        }

        renderCells()
        renderTool()
        gc.fillCircle(PIXEL_WIDTH / 2.0, PIXEL_HEIGHT / 2.0, 20.0)
        gc.restore()

        renderOverlay()
    }

    private fun firstFrame() {
        translateProperty.set(initPoint())
    }

    private fun renderGrid() {
        gc.stroke = Colors.grid

        gc.lineWidth = 2.0
        for (x in 0..PIXEL_WIDTH step GRID_SIZE) {
            gc.strokeLine(x.toDouble(), 0.0, x.toDouble(), PIXEL_HEIGHT.toDouble())
        }
        for (y in 0..PIXEL_HEIGHT step GRID_SIZE) {
            gc.strokeLine(0.0, y.toDouble(), PIXEL_WIDTH.toDouble(), y.toDouble())
        }
    }

    private fun renderCells() {
        gc.stroke = Colors.track
        gc.fill = Colors.track
/*
        for (cell in gridState.getCells()) {
            gc.rotated(cell.rot.degree, cell.getMidPosX().toDouble(), cell.getMidPosY().toDouble()) {
                cell.getRenderer().drawBackground(cell.gridX, cell.gridY, gc, cell)
            }
        }


        gc.fill = Colors.trackHighlight
        for (cell in gridState.getCells()) {
            gc.rotated(cell.rot.degree, cell.getMidPosX().toDouble(), cell.getMidPosY().toDouble()) {
                cell.getRenderer().drawForeground(cell.gridX, cell.gridY, gc, cell)
            }
        }*/
    }

    private fun renderTool() {/*
        gc.fill = Colors.track
        gc.rotated(
            rotation.degree,
            (mouseGridXProperty.get() * GRID_SIZE + GRID_SIZE / 2).toDouble(),
            (mouseGridYProperty.get() * GRID_SIZE + GRID_SIZE / 2).toDouble()
        ) {
            toolboxMode.draw(mouseGridXProperty.get(), mouseGridYProperty.get(), gc, GridView())
        }*/
    }

    private fun clearScreen() {
        gc.fill = Colors.background
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
    }

    private fun calcFps() {
        fpsCounter++
        val elapsedTime = System.currentTimeMillis() - lastTime
        lastTime = System.currentTimeMillis()

        val currentFps = (1.0 / (elapsedTime.toDouble() / 1000.0))
        fpsAvg += currentFps

        if (fpsCounter == 10) {
            lastFpsProperty.set((fpsAvg / (fpsCounter.toDouble())).toInt())
            fpsCounter = 0
            fpsAvg = 0.0
        }
    }

    private fun renderOverlay() {
        gc.font = Font.font(20.0)
        gc.fill = Colors.text
        // Currently no overlay
    }

    private fun initPoint(): Point2D {
        // TODO not working correctly
        var dx = -(PIXEL_WIDTH / 2.0)
        var dy = -(PIXEL_HEIGHT / 2.0)
        val cW = (canvas.width / 2.0) - canvas.parent.layoutX
        val cH = canvas.height / 2.0

        dx += cW
        dy += cH

        return Point2D(dx, dy)
    }

    fun transformScreen(screen: Point2D): Point2D {
        return gridTransform.inverseTransform(screen)
    }

    private fun onClick() {
        toolboxMode.onClick(mouseGridXProperty.get(), mouseGridYProperty.get(), rotation, gridState)
    }

    private fun updateMousePos(mousePos: Point2D) {
        val gridPos = transformScreen(mousePos).multiply(1.0 / GRID_SIZE)
        var x = gridPos.x
        var y = gridPos.y

        if (x < 0.0) {
            x -= 1.0
        }
        if (y < 0.0) {
            y -= 1.0
        }

        mouseGridXProperty.value = x.toInt()
        mouseGridYProperty.value = y.toInt()
    }
}
