package com.github.se7_kn8.xcontrolplus.app

import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font


const val GRID_SPACE = 50
const val WIDTH = GRID_SPACE * 64.0
const val HEIGHT = GRID_SPACE * 64.0

class GridRenderer(private val canvas: Canvas) : AnimationTimer() {

    private var lastTime: Long = 0

    private var fpsAvg = 0.0
    private var fpsCounter = 0
    private var lastFpsAvg = 0.0
    private var gc = canvas.graphicsContext2D;

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
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)


        gc.fill = Color.BLACK
        gc.font = Font.font(10.0 * zoomProperty.get())

        fillText("Time: ", 10.0, 10.0)
        fillText("Name: ", 10.0, 30.0)
        fillText("Desc: ", 10.0, 50.0)
        fillText("FPS: " + lastFpsAvg.toInt(), 10.0, 70.0)
        gc.lineWidth = 2.0
        gc.stroke = Color.GRAY

        for (x in -WIDTH.toInt()..WIDTH.toInt() step GRID_SPACE) {
            strokeLine(x.toDouble(), -HEIGHT, x.toDouble(), HEIGHT)
        }
        for (y in -HEIGHT.toInt()..HEIGHT.toInt() step GRID_SPACE) {
            strokeLine(-WIDTH, y.toDouble(), WIDTH, y.toDouble())
        }

        fillCircle(0.0, 0.0, 10.0)

        gc.fill = Color.RED
        fillCircle(50.0, 50.0, 10.0)

        gc.fill = Color.GREEN
        fillCircle(-50.0, 50.0, 10.0)

        gc.fill = Color.BLUE
        fillCircle(-50.0, -50.0, 10.0)

        gc.fill = Color.YELLOW
        fillCircle(50.0, -50.0, 10.0)

        gc.stroke = Color.BLACK
        gc.lineWidth = 3.0
        strokeLine(-WIDTH, 0.0, WIDTH, 0.0)
        strokeLine(0.0, -HEIGHT, 0.0, HEIGHT)

        gc.fill = Color.PINK
        fillCircle(zoomCenterX, zoomCenterY, 20.0)
        gc.fill = Color.DEEPPINK

    }

    private fun fillCircle(x: Double, y: Double, r: Double) {
        gc.fillOval(transformX(x - r), transformY(y - r), r * 2 * zoomProperty.get(), r * 2 * zoomProperty.get())
    }

    private fun strokeLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        gc.strokeLine(transformX(x1), transformY((y1)), transformX(x2), transformY((y2)))
    }

    private fun fillText(text: String, x: Double, y: Double) {
        gc.fillText(text, transformX(x), transformY(y))
    }

    private fun transformX(x: Double): Double {
        val middleX = (x - zoomCenterX) + ((canvas.width / 2.0) * (1.0 / zoomProperty.get()))
        return middleX * zoomProperty.get()
    }

    private fun transformY(y: Double): Double {
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
