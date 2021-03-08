package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.grid.Colors
import com.github.se7_kn8.xcontrolplus.app.grid.GridCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridContext
import com.github.se7_kn8.xcontrolplus.app.grid.Rotation
import com.github.se7_kn8.xcontrolplus.app.grid.toolbox.ToolboxMode
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.canvas.Canvas


const val GRID_SIZE = 50
const val GRID_WIDTH = 32
const val GRID_HEIGHT = 18
const val PIXEL_WIDTH = GRID_SIZE * GRID_WIDTH
const val PIXEL_HEIGHT = GRID_SIZE * GRID_HEIGHT

class GridRenderer(private val canvas: Canvas) : AnimationTimer() {

    private val gc = GridContext(canvas.graphicsContext2D, this)

    private var lastTime: Long = 0

    private var fpsAvg = 0.0
    private var fpsCounter = 0
    private val cells = ArrayList<GridCell>()

    var toolboxMode = ToolboxMode.MOUSE
    var rotation = Rotation.D0


    var zoomCenterX = 0.0
    var zoomCenterY = 0.0

    val mouseXProperty = SimpleIntegerProperty(1)
    val mouseYProperty = SimpleIntegerProperty(1)

    val zoomProperty = SimpleDoubleProperty(1.0)

    val lastFpsProperty = SimpleIntegerProperty(0)

    override fun handle(now: Long) {
        calcFps()
        clearScreen()



        renderGrid()
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
            cell.getRenderer().render(cell.getGridPosX(), cell.getGridPosY(), cell.getRotation(), gc)
        }
    }

    private fun renderTool() {
        gc.fill = Colors.track
        toolboxMode.draw(getGridX(), getGridY(), rotation, gc)
    }

    private fun clearScreen() {
        gc.fill = Colors.background
        // Draw rect without transformations to clear screen
        canvas.graphicsContext2D.fillRect(0.0, 0.0, canvas.width, canvas.height)
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

    fun transformX(x: Double): Double {
        val middleX = (x - zoomCenterX) + ((canvas.width / 2.0) * (1.0 / zoomProperty.get()))
        return middleX * zoomProperty.get()
    }

    fun transformY(y: Double): Double {
        val middleY = (y - zoomCenterY) + ((canvas.height / 2.0) * (1.0 / zoomProperty.get()))
        return middleY * zoomProperty.get()
    }

    fun transformScreenX(screenX: Double): Double {
        val middleX = screenX - canvas.width / 2.0
        return (middleX / zoomProperty.get()) + zoomCenterX
    }

    fun transformScreenY(screenY: Double): Double {
        val middleY = screenY - canvas.height / 2.0
        return (middleY / zoomProperty.get()) + zoomCenterY
    }

    fun onClick() {
        toolboxMode.onClick(getGridX(), getGridY(), rotation, cells)
    }

    fun getGridX(): Int {
        var mouseX = mouseXProperty.get()

        if (mouseX > 0) {
            mouseX -= 1
        }
        return mouseX
    }

    fun getGridY(): Int {
        var mouseY = mouseYProperty.get()


        if (mouseY > 0) {
            mouseY -= 1
        }
        return mouseY
    }

}
