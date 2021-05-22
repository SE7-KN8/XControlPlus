package com.github.se7_kn8.xcontrolplus.app.grid

import com.github.se7_kn8.xcontrolplus.app.actions.Action
import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.github.se7_kn8.xcontrolplus.gridview.CellRotation
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import javafx.stage.Stage
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

// Maybe split this class into smaller classes and use DI?
class GridState(val gridView: GridView<BaseCell>) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(BaseCell::class.java, AbstractClassAdapter<BaseCell>())
        .setPrettyPrinting()
        .create()

    private val type = object : TypeToken<ArrayList<BaseCell>>() {}.type!!

    val contextMenu = GridContextMenu(this)

    var userRotation = CellRotation.D0

    fun getCells(): List<BaseCell> = gridView.cells

    fun removeCell(baseCell: BaseCell) = gridView.cells.remove(baseCell)


    fun addCell(baseCell: BaseCell) {
        getCell(baseCell.gridX, baseCell.gridY).ifPresent {
            removeCell(it)
        }
        gridView.cells.add(baseCell)
    }

    fun getCell(x: Int, y: Int) = gridView.findCell(x, y)

    fun getHoveredCell() = gridView.findCell(mouseGridX(), mouseGridY())

    fun getSelectedCell(): BaseCell? = gridView.selectedCell

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

    fun doAction(action: Action) {
        action.init(this)
        if (action.valid(this)) {
            action.doAction(this)
        }
    }

    fun saveToFile(stage: Stage) {
        FileUtil.saveFileChooser(stage, FileUtil.PROJECT_FILE) {
            val content = saveCells()
            FileUtil.writeStringToFile(it, content)
        }
    }

    fun loadFromFile(stage: Stage) {
        FileUtil.openFileChooser(stage, FileUtil.PROJECT_FILE) { path ->
            FileUtil.readFileToString(path)?.also {
                loadCells(it)
            }

        }
    }

}
