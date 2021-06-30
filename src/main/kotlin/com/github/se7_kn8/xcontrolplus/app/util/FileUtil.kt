package com.github.se7_kn8.xcontrolplus.app.util

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import javafx.scene.image.Image
import javafx.stage.FileChooser
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileUtil {

    val PROJECT_FILE = FileChooser.ExtensionFilter("X-Control Plus Project (*.xcpp)", "*.xcpp")

    fun readFileToString(path: Path?): String? {
        debug("Reading content from file $path")
        try {
            if (path != null && Files.exists(path)) {
                Files.newBufferedReader(path, Charset.forName("UTF-8")).use {
                    return it.readText()
                }
            }
        } catch (e: Exception) {
            warn(e, "Error while reading file")
        }
        return null
    }

    fun writeStringToFile(path: Path?, content: String?) {
        debug("Write content to file $path")
        if (path != null) {
            try {
                Files.createDirectories(path.parent)
                Files.writeString(path, content, Charset.forName("UTF-8"))
            } catch (e: Exception) {
                warn(e, "Error while writing to file")
            }
        }
    }

    fun getBasePath() = Paths.get(System.getProperty("user.home"), ".xcontrolplus").also { Files.createDirectories(it) }

    fun getSpecificPath(name: String) = getBasePath().resolve(name).also { Files.createDirectories(it) }

    fun openFileChooser(vararg extensions: FileChooser.ExtensionFilter, onFile: (Path) -> Unit) {
        val chooser = FileChooser()
        chooser.extensionFilters.addAll(extensions)
        chooser.initialDirectory = File(ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_OPEN_PATH]).let {
            if (it.isDirectory) {
                it
            } else {
                File(System.getProperty("user.home"))
            }
        }
        val result = chooser.showOpenDialog(WindowContext.get().primaryStage)
        if (result != null) {
            val path = result.toPath()
            ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_OPEN_PATH] = path.parent.toString()
            onFile(path)
        }
    }

    fun saveFileChooser(vararg extensions: FileChooser.ExtensionFilter, onFile: (Path) -> Unit) {
        val chooser = FileChooser()
        chooser.extensionFilters.addAll(extensions)
        chooser.initialDirectory = File(ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_SAVE_PATH]).let {
            if (it.isDirectory) {
                it
            } else {
                File(System.getProperty("user.home"))
            }
        }
        val result = chooser.showSaveDialog(WindowContext.get().primaryStage)
        if (result != null) {
            val path = result.toPath()
            ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_SAVE_PATH] = path.parent.toString()
            onFile(path)
        }
    }

    fun getResource(path: String): URL {
        debug("Loading resource $path")
        return FileUtil::class.java.getResource("/$path")!!
    }

    fun getResourceAsStream(path: String): InputStream {
        debug("Loading resource stream $path")
        return FileUtil::class.java.getResourceAsStream("/$path")!!
    }

    fun getAsset(path: String) = getResource("assets/$path")

    fun getAssetAsStream(path: String) = getResourceAsStream("assets/$path")

    fun getImage(path: String) = Image(getAssetAsStream(path))
}
