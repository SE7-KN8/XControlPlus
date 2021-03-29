package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.grid.Colors
import com.github.se7_kn8.xcontrolplus.app.grid.GridCell
import com.github.se7_kn8.xcontrolplus.app.grid.Rotation
import com.github.se7_kn8.xcontrolplus.app.grid.toolbox.ToolboxMode
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.transform.Affine


const val GRID_SIZE = 32
const val GRID_WIDTH = 32
const val GRID_HEIGHT = 18
const val PIXEL_WIDTH = GRID_SIZE * GRID_WIDTH
const val PIXEL_HEIGHT = GRID_SIZE * GRID_HEIGHT

class GridRenderer(private val canvas: Canvas) : AnimationTimer() {

    private val gc = canvas.graphicsContext2D

    private var lastTime: Long = 0

    private var fpsAvg = 0.0
    private var fpsCounter = 0
    private val cells = ArrayList<GridCell>()

    var toolboxMode = ToolboxMode.MOUSE
    var rotation = Rotation.D0

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
            val newScale = gc.transform.apply {
                appendScale(
                    scaleFactor,
                    scaleFactor,
                    middlePoint.x,
                    middlePoint.y
                )
            }
            gc.transform = newScale
        }
        translateProperty.addListener { _, _, newValue ->
            val newTranslation = gc.transform.apply {
                tx = newValue.x
                ty = newValue.y
            }
            gc.transform = newTranslation
        }
    }

    override fun handle(now: Long) {
        calcFps()
        clearScreen()


        if (showGridProperty.get()) {
            renderGrid()
        }

        renderCells()
        renderTool()
    }

    private fun renderGrid() {
        gc.stroke = Colors.grid

        gc.lineWidth = 2.0
        for (x in -PIXEL_WIDTH..PIXEL_WIDTH step GRID_SIZE) {
            gc.strokeLine(x.toDouble(), -PIXEL_HEIGHT.toDouble(), x.toDouble(), PIXEL_HEIGHT.toDouble())
        }
        for (y in -PIXEL_HEIGHT..PIXEL_HEIGHT step GRID_SIZE) {
            gc.strokeLine(-PIXEL_WIDTH.toDouble(), y.toDouble(), PIXEL_WIDTH.toDouble(), y.toDouble())
        }

        gc.lineWidth = 5.0
        gc.strokeLine(-PIXEL_WIDTH.toDouble(), 0.0, PIXEL_WIDTH.toDouble(), 0.0)
        gc.strokeLine(0.0, -PIXEL_HEIGHT.toDouble(), 0.0, PIXEL_HEIGHT.toDouble())
    }

    private fun renderCells() {
        gc.stroke = Colors.track
        gc.fill = Colors.track

        for (cell in cells) {
            gc.rotated(cell.getRotation().degree, cell.getMidPosX().toDouble(), cell.getMidPosY().toDouble()) {
                cell.getRenderer().drawBackground(cell.getGridPosX(), cell.getGridPosY(), gc, cell)
            }
        }


        gc.fill = Colors.trackHighlight
        for (cell in cells) {
            gc.rotated(cell.getRotation().degree, cell.getMidPosX().toDouble(), cell.getMidPosY().toDouble()) {
                cell.getRenderer().drawForeground(cell.getGridPosX(), cell.getGridPosY(), gc, cell)
            }
        }
    }

    private fun renderTool() {
        gc.fill = Colors.track
        gc.rotated(
            rotation.degree,
            (mouseGridXProperty.get() * GRID_SIZE + GRID_SIZE / 2).toDouble(),
            (mouseGridYProperty.get() * GRID_SIZE + GRID_SIZE / 2).toDouble()
        ) {
            toolboxMode.draw(mouseGridXProperty.get(), mouseGridYProperty.get(), gc)

        }
    }

    private fun clearScreen() {
        val oldAffine = gc.transform
        // Reset matrix to fill complete screen
        gc.transform = Affine()
        gc.fill = Colors.background
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
        gc.transform = oldAffine
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

    fun transformScreen(screen: Point2D): Point2D {
        return gc.transform.inverseTransform(screen)
    }

    fun onClick() {
        toolboxMode.onClick(mouseGridXProperty.get(), mouseGridYProperty.get(), rotation, cells)
    }
}

fun GraphicsContext.rotateAround(degree: Double, midPosX: Double, midPosY: Double) {
    transform = transform.apply {
        appendRotation(
            degree,
            midPosX,
            midPosY
        )
    }
}

fun GraphicsContext.rotated(degree: Double, midPosX: Double, midPosY: Double, handler: () -> Unit) {
    save()
    rotateAround(degree, midPosX, midPosY)
    handler()
    restore()
}
