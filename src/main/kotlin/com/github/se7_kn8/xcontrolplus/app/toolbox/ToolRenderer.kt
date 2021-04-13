package com.github.se7_kn8.xcontrolplus.app.toolbox

import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridState
import com.github.se7_kn8.xcontrolplus.app.util.rotated
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.beans.property.SimpleObjectProperty

class ToolRenderer(private val gridView: GridView<BaseCell>, private val gridState: GridState) {

    val currentTool = SimpleObjectProperty(ToolboxMode.MOUSE)

    init {
        gridView.isClickAndDrag = true
        gridView.setForegroundCallback { _, renderer ->
            renderer.gc.rotated(
                gridState.userRotation.rotation(),
                renderer.getMidX(gridView.mouseGridX.toDouble()),
                renderer.getMidY(gridView.mouseGridY.toDouble())
            ) {
                currentTool.value.draw(gridView.mouseGridX, gridView.mouseGridY, renderer.gc, gridView)
            }
        }

        gridView.setClickCallback { event, drag ->
            gridState.contextMenu.hide()
            if (drag && currentTool.value.allowDrag()) {
                currentTool.value.onClick(event, gridState)
            } else if (!drag) {
                currentTool.value.onClick(event, gridState)
            }
        }
    }

}
