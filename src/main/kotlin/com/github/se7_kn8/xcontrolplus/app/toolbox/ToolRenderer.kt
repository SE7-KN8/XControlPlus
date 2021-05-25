package com.github.se7_kn8.xcontrolplus.app.toolbox

import com.github.se7_kn8.xcontrolplus.app.grid.GridHelper
import com.github.se7_kn8.xcontrolplus.app.util.rotated
import javafx.beans.property.SimpleObjectProperty

class ToolRenderer {

    val currentTool = SimpleObjectProperty(Tool.MOUSE)

    fun detach(gridHelper: GridHelper) {
        gridHelper.gridView.foregroundCallback = null
        gridHelper.gridView.clickCallback = null
    }

    fun attach(gridHelper: GridHelper) {
        gridHelper.gridView.isClickAndDrag = true
        gridHelper.gridView.isHighlightSelectedCell = true
        gridHelper.gridView.setForegroundCallback { _, renderer ->
            renderer.gc.rotated(
                gridHelper.toolRotation.rotation(),
                renderer.getMidX(gridHelper.mouseGridX().toDouble()),
                renderer.getMidY(gridHelper.mouseGridY().toDouble())
            ) {
                currentTool.value.draw(gridHelper.mouseGridX(), gridHelper.mouseGridY(), renderer.gc, gridHelper.gridView)
            }
        }

        gridHelper.gridView.setClickCallback { event, drag ->
            gridHelper.contextMenu.hide()
            if (drag && currentTool.value.allowDrag) {
                currentTool.value.onClick(event, gridHelper)
            } else if (!drag) {
                currentTool.value.onClick(event, gridHelper)
            }
        }
    }

}
