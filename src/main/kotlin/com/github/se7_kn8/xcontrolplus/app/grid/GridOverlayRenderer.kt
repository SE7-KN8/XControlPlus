package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.util.translate
import javafx.scene.text.Font

class GridOverlayRenderer() {

    fun detach(gridHelper: GridHelper) {
        gridHelper.gridView.overlayCallback = null
    }

    fun attach(gridHelper: GridHelper) {
        gridHelper.gridView.setOverlayCallback { _, renderer ->
            if (ApplicationContext.get().connectionHandler.trackStop.get()) {
                renderer.gc.fill = Colors.stop
                renderer.gc.fillRect(0.0, 0.0, gridHelper.gridView.width, 60.0)
                renderer.gc.fill = Colors.text
                renderer.gc.font = Font.font(20.0)
                renderer.gc.fillText(translate("overlay.track_power_off"), gridHelper.gridView.width / 2.0 - 120.0, 40.0)
            }
        }
    }


}
