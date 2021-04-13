package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.gridview.CellRotation
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.FileReader
import java.io.PrintWriter
import java.lang.reflect.Type

// From: https://stackoverflow.com/a/9550086/10648509
class AbstractClassAdapter<T : Any> : JsonSerializer<T>, JsonDeserializer<T> {

    override fun serialize(src: T, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement {
        val wrapper = JsonObject()
        wrapper.addProperty("type", src::class.java.simpleName)
        wrapper.add("data", context.serialize(src))
        return wrapper
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): T {
        val wrapper = json.asJsonObject
        val typeName = wrapper.get("type")
        val data = wrapper.get("data")
        val actualType = typeForName(typeName)
        return context.deserialize(data, actualType)
    }

    private fun typeForName(element: JsonElement): Type {
        try {
            return Class.forName(this::class.java.`package`.name + "." + element.asString)
        } catch (e: Exception) {
            throw JsonParseException(e)
        }
    }

}

class GridState(val gridView: GridView<BaseCell>) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(BaseCell::class.java, AbstractClassAdapter<BaseCell>())
        .setPrettyPrinting()
        .create()

    val contextMenu = GridContextMenu(this)

    var userRotation = CellRotation.D0

    val type = object : TypeToken<ArrayList<BaseCell>>() {}.type

    fun getCells(): List<BaseCell> = gridView.cells

    fun removeCell(baseCell: BaseCell) = gridView.cells.remove(baseCell)

    fun addCell(baseCell: BaseCell) = gridView.cells.add(baseCell)

    fun getCell(x: Int, y: Int) = gridView.findCell(x, y)

    fun getCurrentCell() = gridView.findCell(mouseGridX(), mouseGridY())

    fun mouseGridX() = gridView.mouseGridX
    fun mouseGridY() = gridView.mouseGridY

    fun loadCells(from: String) {
        val newData = gson.fromJson<ArrayList<BaseCell>>(from, type)
        gridView.cells.clear()
        gridView.cells.addAll(newData)
    }

    fun saveCells(): String {
        val data: ArrayList<BaseCell> = ArrayList(gridView.cells)
        return gson.toJson(data, type)
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
