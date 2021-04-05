package com.github.se7_kn8.xcontrolplus.app.toolbox

import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.grid.GridState
import com.github.se7_kn8.xcontrolplus.app.util.rotated
import com.github.se7_kn8.xcontrolplus.gridview.CellRotation
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.beans.property.SimpleObjectProperty

class ToolRenderer(private val gridView: GridView<BaseCell>, private val gridState: GridState) {

    val currentTool = SimpleObjectProperty(ToolboxMode.MOUSE)
    var rotation = CellRotation.D0

    init {
        gridView.setForegroundCallback { _, renderer ->
            renderer.gc.rotated(
                rotation.rotation(),
                renderer.getMidX(gridView.mouseGridX.toDouble()),
                renderer.getMidY(gridView.mouseGridY.toDouble())
            ) {
                currentTool.value.draw(gridView.mouseGridX, gridView.mouseGridY, renderer.gc, gridView)
            }
        }

        gridView.setClickCallback {
            currentTool.value.onClick(gridView.mouseGridX, gridView.mouseGridY, rotation, gridState);
        }
    }

}
