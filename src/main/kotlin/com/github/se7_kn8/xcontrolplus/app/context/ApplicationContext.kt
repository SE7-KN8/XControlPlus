package com.github.se7_kn8.xcontrolplus.app.context

import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import com.github.se7_kn8.xcontrolplus.app.settings.UserSettings
import com.google.gson.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
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
            return Class.forName(BaseCell::class.java.`package`.name + "." + element.asString)
        } catch (e: Exception) {
            throw JsonParseException(e)
        }
    }
}

class SimpleStringPropertyAdapter : JsonSerializer<SimpleStringProperty>, JsonDeserializer<SimpleStringProperty> {
    override fun serialize(p0: SimpleStringProperty?, p1: Type?, p2: JsonSerializationContext?) = JsonPrimitive(p0?.get())

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?) = SimpleStringProperty(p0?.asString)
}

class SimpleIntegerPropertyAdapter : JsonSerializer<SimpleIntegerProperty>, JsonDeserializer<SimpleIntegerProperty> {
    override fun serialize(p0: SimpleIntegerProperty?, p1: Type?, p2: JsonSerializationContext?) = JsonPrimitive(p0?.get())

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?) = SimpleIntegerProperty(p0?.asInt!!)
}

class SimpleBooleanPropertyAdapter : JsonSerializer<SimpleBooleanProperty>, JsonDeserializer<SimpleBooleanProperty> {
    override fun serialize(p0: SimpleBooleanProperty?, p1: Type?, p2: JsonSerializationContext?) = JsonPrimitive(p0?.get())

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?) = SimpleBooleanProperty(p0?.asBoolean!!)
}

class ApplicationContext() {

    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(BaseCell::class.java, AbstractClassAdapter<BaseCell>())
        .registerTypeAdapter(SimpleStringProperty::class.java, SimpleStringPropertyAdapter())
        .registerTypeAdapter(SimpleIntegerProperty::class.java, SimpleIntegerPropertyAdapter())
        .registerTypeAdapter(SimpleBooleanProperty::class.java, SimpleBooleanPropertyAdapter())
        .create()

    val applicationSettings = ApplicationSettings()
    val userSettings = UserSettings()

    companion object {

        private lateinit var instance: ApplicationContext
        private var init = false;

        fun init() {
            if (!init) {
                init = true
                instance = ApplicationContext()
            }
        }

        fun get() = instance
    }

}
