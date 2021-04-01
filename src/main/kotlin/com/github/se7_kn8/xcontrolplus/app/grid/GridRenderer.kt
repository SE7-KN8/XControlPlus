package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.toolbox.ToolboxMode
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
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
            gridTransform.appendScale(
                scaleFactor,
                scaleFactor,
                middlePoint.x,
                middlePoint.y
            )
        }
        translateProperty.addListener { _, _, newValue ->
            gridTransform.tx = newValue.x
            gridTransform.ty = newValue.y
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

        for (cell in gridState.cells) {
            gc.rotated(cell.rot.degree, cell.getMidPosX().toDouble(), cell.getMidPosY().toDouble()) {
                cell.getRenderer().drawBackground(cell.gridX, cell.gridY, gc, cell)
            }
        }


        gc.fill = Colors.trackHighlight
        for (cell in gridState.cells) {
            gc.rotated(cell.rot.degree, cell.getMidPosX().toDouble(), cell.getMidPosY().toDouble()) {
                cell.getRenderer().drawForeground(cell.gridX, cell.gridY, gc, cell)
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
        gc.fillText("Test 123", 100.0, 100.0)
    }

    private fun initPoint(): Point2D {
        var dx = -(PIXEL_WIDTH / 2.0)
        var dy = -(PIXEL_HEIGHT / 2.0)

        dx += canvas.width
        dy += canvas.height / 2.0

        return Point2D(dx, dy)
    }

    fun transformScreen(screen: Point2D): Point2D {
        return gridTransform.inverseTransform(screen)
    }

    fun onClick() {
        toolboxMode.onClick(mouseGridXProperty.get(), mouseGridYProperty.get(), rotation, gridState)
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

fun GraphicsContext.fillCircle(x: Double, y: Double, r: Double) {
    fillOval(x - r, y - r, 2 * r, 2 * r)
}
