package com.github.se7_kn8.xcontrolplus.app.context

import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import com.github.se7_kn8.xcontrolplus.app.settings.UserSettings
import com.google.gson.*
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

class ApplicationContext() {

    val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(BaseCell::class.java, AbstractClassAdapter<BaseCell>())
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
