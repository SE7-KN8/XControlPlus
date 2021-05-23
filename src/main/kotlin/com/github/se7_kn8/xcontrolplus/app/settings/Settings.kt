package com.github.se7_kn8.xcontrolplus.app.settings

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.google.gson.reflect.TypeToken

class SettingsEntry<T>(val saveName: String, val defaultValue: T)

abstract class Settings(fileName: String, val entries: List<SettingsEntry<*>>) {
    private val type = object : TypeToken<HashMap<String, Any>>() {}.type!!
    val settings = HashMap<String, Any>()
    private var hasChanged = false

    private val filePath = FileUtil.getSpecificPath("settings").resolve("${fileName}.json")

    fun load() {
        val newData = loadFileIntoMap()
        settings.clear()
        settings.putAll(newData)

        // If settings are missing add them to the config file with default values
        var shouldSave = false
        entries.forEach {
            if (settings[it.saveName] == null) {
                shouldSave = true
                settings[it.saveName] = it.defaultValue as Any
            }
        }
        if (shouldSave) {
            save(true)
        }
    }

    fun save(forceSave: Boolean = false) {
        if (hasChanged || forceSave) {
            saveMapIntoFile()
        }
    }

    private fun loadFileIntoMap(): HashMap<String, Any> {
        FileUtil.readFileToString(filePath)?.also {
            return ApplicationContext.get().gson.fromJson(it, type)
        }
        return HashMap()
    }

    private fun saveMapIntoFile() {
        val json = ApplicationContext.get().gson.toJson(settings, type)
        FileUtil.writeStringToFile(filePath, json)
    }

    inline operator fun <reified T> get(entry: SettingsEntry<T>): T {
        val value = settings.getOrPut(entry.saveName) { entry.defaultValue as Any }
        if (value is T) {
            return value
        }
        return entry.defaultValue
    }

    fun getAny(entry: SettingsEntry<*>): Any {
        return settings.getOrPut(entry.saveName) { entry.defaultValue as Any }
    }


    operator fun <T> set(entry: SettingsEntry<T>, value: T) {
        settings[entry.saveName] = value as Any
        hasChanged = true
    }

    fun setAny(entry: SettingsEntry<*>, value: Any) {
        settings[entry.saveName] = value
        hasChanged = true
    }

}

// Application settings will be not visible to the user
class ApplicationSettings : Settings(
    "application",
    listOf(
        LATEST_OPEN_PATH,
        LATEST_SAVE_PATH,
        START_MAXIMIZED,
        WINDOW_X,
        WINDOW_Y,
        WINDOW_WIDTH,
        WINDOW_HEIGHT
    )
) {
    companion object {
        val LATEST_OPEN_PATH = SettingsEntry("latest_open_path", "")
        val LATEST_SAVE_PATH = SettingsEntry("latest_save_path", "")
        val START_MAXIMIZED = SettingsEntry("start_maximized", false)
        val WINDOW_X = SettingsEntry("window_x", 0.0)
        val WINDOW_Y = SettingsEntry("window_y", 0.0)
        val WINDOW_WIDTH = SettingsEntry("window_width", 0.0)
        val WINDOW_HEIGHT = SettingsEntry("window_height", 0.0)
    }
}

// User settings are displayed in the settings menu and can be edited by the user
class UserSettings : Settings(
    "user",
    listOf(
        ASK_BEFORE_EXIT
    )
) {
    companion object {
        val ASK_BEFORE_EXIT = SettingsEntry("ask_before_exit", true)
    }
}
