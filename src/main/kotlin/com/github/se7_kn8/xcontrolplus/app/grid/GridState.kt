package com.github.se7_kn8.xcontrolplus.app.grid

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.FileReader
import java.io.PrintWriter
import java.lang.Exception
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

class GridState {

    private val gson = GsonBuilder()
        .registerTypeAdapter(GridCell::class.java, AbstractClassAdapter<GridCell>())
        .setPrettyPrinting()
        .create()
    val cells = ArrayList<GridCell>()

    val type = object : TypeToken<ArrayList<GridCell>>() {}.type

    fun loadCells(from: String) {
        val newData = gson.fromJson<ArrayList<GridCell>>(from, type)
        cells.clear()
        cells.addAll(newData)
    }

    fun saveCells(): String {
        val data: ArrayList<GridCell> = cells
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
