package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.GridRenderer
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint
import javafx.scene.shape.ArcType
import javafx.scene.text.Font

class GridContext(private val gc: GraphicsContext, private val renderer: GridRenderer) {

    var fill: Paint
        get() = gc.fill
        set(value) {
            gc.fill = value
        }

    var stroke: Paint
        get() = gc.stroke
        set(value) {
            gc.stroke = value
        }

    var fontSize: Double
        get() = gc.font.size
        set(value) {
            gc.font = Font(value * renderer.zoomProperty.get())
        }

    var lineWidth: Double
        get() = gc.lineWidth
        set(value) {
            gc.lineWidth = value * renderer.zoomProperty.get()
        }

    fun fillRect(x1: Double, y1: Double, w: Double, h: Double) {
        gc.fillRect(renderer.transformX(x1), renderer.transformY(y1), w * renderer.zoomProperty.get(), h * renderer.zoomProperty.get())
    }

    fun fillCircle(x: Double, y: Double, r: Double) {
        gc.fillOval(renderer.transformX(x - r), renderer.transformY(y - r), r * 2 * renderer.zoomProperty.get(), r * 2 * renderer.zoomProperty.get())
    }

    fun strokeLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        gc.strokeLine(renderer.transformX(x1), renderer.transformY((y1)), renderer.transformX(x2), renderer.transformY((y2)))
    }

    fun fillText(text: String, x: Double, y: Double) {
        gc.fillText(text, renderer.transformX(x), renderer.transformY(y))
    }

    fun fillArc(x: Double, y: Double, w: Double, h: Double, startAngle: Double, endAngle: Double, type: ArcType) {
        gc.fillArc(
            renderer.transformX(x),
            renderer.transformY(y),
            w * renderer.zoomProperty.get(),
            h * renderer.zoomProperty.get(),
            startAngle,
            endAngle,
            type
        )
    }

    fun fillPolygon(xPoints: DoubleArray, yPoints: DoubleArray, points: Int) {
        for ((index, xPoint) in xPoints.withIndex()) {
            xPoints[index] = renderer.transformX(xPoint)
        }
        for ((index, yPoint) in yPoints.withIndex()) {
            yPoints[index] = renderer.transformY(yPoint)
        }
        gc.fillPolygon(xPoints, yPoints, points)
    }

}
