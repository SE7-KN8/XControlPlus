package com.github.se7_kn8.xcontrolplus.app.grid

import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.FileReader
import java.io.PrintWriter

class GridState {

    val cells = ArrayList<GridCell>()

    fun loadCells(from: String) {
        val newData = Json.decodeFromString<ArrayList<GridCell>>(from)
        cells.clear()
        cells.addAll(newData)
    }

    fun saveCells(): String {
        val data: ArrayList<GridCell> = cells
        return Json {
            prettyPrint = true
        }.encodeToString(data)
    }

    fun saveToFile(stage: Stage) {
        val chooser = createFileChooser()
        val file = chooser.showSaveDialog(stage)
        if (file != null) {
            val writer = PrintWriter(file)
            writer.write(saveCells())
            writer.flush()
            writer.close()
        }
    }

    fun loadFromFile(stage: Stage) {
        val chooser = createFileChooser()
        val file = chooser.showOpenDialog(stage)
        if (file != null && file.exists()) {
            val reader = FileReader(file)
            val data = reader.readText()
            reader.close()
            loadCells(data)
        }
    }


    private fun createFileChooser(): FileChooser {
        val chooser = FileChooser()
        val filter = FileChooser.ExtensionFilter("X-Control Plus Files (*.xctrlp)", "*.xctrlp")
        chooser.extensionFilters.add(filter)
        return chooser
    }

}
