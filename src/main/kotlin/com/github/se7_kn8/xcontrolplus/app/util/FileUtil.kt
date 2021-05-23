package com.github.se7_kn8.xcontrolplus.app.util

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import javafx.scene.image.Image
import javafx.stage.FileChooser
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileUtil {

    val PROJECT_FILE = FileChooser.ExtensionFilter("X-Control Plus Project (*.xctrlp)", "*.xctrlp")

    fun readFileToString(path: Path?): String? {
        try {
            if (path != null && Files.exists(path)) {
                val reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))
                return reader.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while loading file")
        }
        return null
    }

    fun writeStringToFile(path: Path?, content: String?) {
        if (path != null) {
            try {
                Files.createDirectories(path.parent)
                Files.writeString(path, content, Charset.forName("UTF-8"))
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error while writing to file")
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

    fun getAsset(path: String) = FileUtil::class.java.getResource("/assets/$path")

    fun getAssetAsStream(path: String) = FileUtil::class.java.getResourceAsStream("/assets/$path")

    fun getImage(path: String) = Image(getAssetAsStream(path))

}
