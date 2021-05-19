package com.github.se7_kn8.xcontrolplus.app.toolbox

import com.github.se7_kn8.xcontrolplus.app.grid.GridState
import com.github.se7_kn8.xcontrolplus.app.util.rotated
import javafx.beans.property.SimpleObjectProperty

class ToolRenderer(private val gridState: GridState) {

    val currentTool = SimpleObjectProperty(ToolboxMode.MOUSE)

    init {
        gridState.gridView.isClickAndDrag = true
        gridState.gridView.setForegroundCallback { _, renderer ->
            renderer.gc.rotated(
                gridState.userRotation.rotation(),
                renderer.getMidX(gridState.mouseGridX().toDouble()),
                renderer.getMidY(gridState.mouseGridY().toDouble())
            ) {
                currentTool.value.draw(gridState.mouseGridX(), gridState.mouseGridY(), renderer.gc, gridState.gridView)
            }
        }

        gridState.gridView.setClickCallback { event, drag ->
            gridState.contextMenu.hide()
            if (drag && currentTool.value.allowDrag()) {
                currentTool.value.onClick(event, gridState)
            } else if (!drag) {
                currentTool.value.onClick(event, gridState)
            }
        }
    }

}
