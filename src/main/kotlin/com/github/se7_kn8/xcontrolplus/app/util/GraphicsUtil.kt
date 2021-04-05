package com.github.se7_kn8.xcontrolplus.app.util

import javafx.scene.canvas.GraphicsContext

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

