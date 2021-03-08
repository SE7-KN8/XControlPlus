package com.github.se7_kn8.xcontrolplus.app

import com.github.se7_kn8.xcontrolplus.app.grid.GridCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridContext
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font


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
    private var lastFpsAvg = 0.0
    private var grid = Array(GRID_WIDTH * 2) { Array(GRID_HEIGHT * 2) { GridCell() } }

    val zoomProperty = SimpleDoubleProperty(1.0)

    var zoomCenterX = 0.0
    var zoomCenterY = 0.0

    override fun handle(now: Long) {

        fpsCounter++
        val elapsedTime = System.currentTimeMillis() - lastTime
        lastTime = System.currentTimeMillis()

        val currentFps = (1.0 / (elapsedTime.toDouble() / 1000.0))
        fpsAvg += currentFps

        if (fpsCounter == 10) {
            lastFpsAvg = fpsAvg / (fpsCounter.toDouble())
            fpsCounter = 0
            fpsAvg = 0.0
        }

        gc.fill = Color.WHITE
        // Draw rect without transformations to clear screen
        canvas.graphicsContext2D.fillRect(0.0, 0.0, canvas.width, canvas.height)

        for (x in -GRID_WIDTH until GRID_WIDTH) {
            for (y in -GRID_HEIGHT until GRID_HEIGHT) {
                val xWidth = (x.toDouble() + GRID_WIDTH) / (GRID_WIDTH * 2.0)
                val yWidth = (y.toDouble() + GRID_HEIGHT) / (GRID_HEIGHT * 2.0)
                gc.fill = grid[x + GRID_WIDTH][y + GRID_HEIGHT].getFill()
                gc.fillRect(x.toDouble() * GRID_SIZE, y.toDouble() * GRID_SIZE, GRID_SIZE.toDouble(), GRID_SIZE.toDouble())
            }
        }

        gc.fill = Color.BLACK
        gc.fontSize = 10.0

        gc.fillText("Time: ", 10.0, 10.0)
        gc.fillText("Name: ", 10.0, 30.0)
        gc.fillText("Desc: ", 10.0, 50.0)
        gc.fillText("FPS: " + lastFpsAvg.toInt(), 10.0, 70.0)
        gc.lineWidth = 2.0
        gc.stroke = Color.GRAY

        for (x in -PIXEL_WIDTH..PIXEL_WIDTH step GRID_SIZE) {
            gc.strokeLine(x.toDouble(), -PIXEL_HEIGHT.toDouble(), x.toDouble(), PIXEL_HEIGHT.toDouble())
        }
        for (y in -PIXEL_HEIGHT..PIXEL_HEIGHT step GRID_SIZE) {
            gc.strokeLine(-PIXEL_WIDTH.toDouble(), y.toDouble(), PIXEL_WIDTH.toDouble(), y.toDouble())
        }

        gc.stroke = Color.BLACK
        gc.lineWidth = 3.0
        gc.strokeLine(-PIXEL_WIDTH.toDouble(), 0.0, PIXEL_WIDTH.toDouble(), 0.0)
        gc.strokeLine(0.0, -PIXEL_HEIGHT.toDouble(), 0.0, PIXEL_HEIGHT.toDouble())
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

}
