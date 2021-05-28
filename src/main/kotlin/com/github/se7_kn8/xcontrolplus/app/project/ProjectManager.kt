package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.beans.property.SimpleObjectProperty
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

enum class SaveVersion {
    VERSION_1_0
}

class ProjectMetadata {
    val version = SaveVersion.VERSION_1_0
}

class ProjectManager {
    val metadataFileName = "metadata.json"
    val activeProject = SimpleObjectProperty<Project>()

    fun loadProject() {
        // TODO check for unsaved project
        FileUtil.openFileChooser(FileUtil.PROJECT_FILE) { path ->
            loadProject(path.toString())
        }
    }

    fun loadLatestProject() {
        val path = Path.of(ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_PROJECT_PATH])
        if (path.toString().isNotBlank() && Files.exists(path) && Files.isReadable(path)) {
            loadProject(ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_PROJECT_PATH])
        }
    }

    private fun loadProject(path: String) {
        ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_PROJECT_PATH] = path
        ZipFile(path).use { file ->
            val entries = file.entries().toList().sortedBy { it.name }.associateBy { it.name }.toMutableMap()
            val metadataEntry = entries[metadataFileName] ?: throw IllegalStateException("Project file is invalid")
            InputStreamReader(file.getInputStream(metadataEntry), Charsets.UTF_8).use { metadataReader ->
                val metadata = ApplicationContext.get().gson.fromJson(metadataReader, ProjectMetadata::class.java)
                if (metadata.version != ProjectMetadata().version) {
                    throw IllegalStateException("Unsupported save version")
                }
                val project = Project(path)
                entries.remove(metadataFileName)
                for (entry in entries.values) {
                    InputStreamReader(file.getInputStream(entry), Charsets.UTF_8).use { sheetReader ->
                        val sheet = Sheet.load(sheetReader.readText())
                        project.sheets.add(sheet)
                    }
                }
                activeProject.set(project)
            }
        }
    }

    fun saveProject() {
        activeProject.get()?.let { project ->
            val charset = Charset.forName("UTF-8")
            FileUtil.saveFileChooser(FileUtil.PROJECT_FILE) { path ->
                ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_PROJECT_PATH] = path.toString()
                ZipOutputStream(FileOutputStream(path.toFile())).use { zip ->
                    val metadataEntry = ZipEntry(metadataFileName)
                    zip.putNextEntry(metadataEntry)
                    val metadataBytes = ApplicationContext.get().gson.toJson(ProjectMetadata()).toByteArray(charset)
                    zip.write(metadataBytes, 0, metadataBytes.size)
                    zip.closeEntry()
                    var counter = 1
                    for (sheet in project.sheets) {
                        val sheetEntry = ZipEntry("sheet-$counter.json")
                        zip.putNextEntry(sheetEntry)
                        val sheetBytes = sheet.save().toByteArray(charset)
                        zip.write(sheetBytes, 0, sheetBytes.size)
                        zip.closeEntry()
                        counter += 1
                    }
                }
            }
        }
    }

    fun newProject() {
        activeProject.set(Project("Unsaved Project"))
        activeProject.get().sheets.add(Sheet("Sheet 1", GridView<BaseCell>()))
    }

    fun closeProject() {
        activeProject.set(null)
        ApplicationContext.get().connectionHandler.clear()
    }
}
