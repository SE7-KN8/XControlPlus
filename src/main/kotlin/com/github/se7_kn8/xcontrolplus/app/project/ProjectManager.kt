package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.settings.ApplicationSettings
import com.github.se7_kn8.xcontrolplus.app.util.*
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

enum class SaveVersion(val versionName: String, val compatibleTo: List<SaveVersion> = emptyList()) {
    VERSION_1_0("1.0"),
    VERSION_1_1("1.1", listOf(VERSION_1_0))
}

data class ProjectMetadata(val version: SaveVersion = SaveVersion.VERSION_1_1)

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
        debug("Trying to load project from $path")
        ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_PROJECT_PATH] = path
        ZipFile(path).use { file ->
            val entries = file.entries().toList().sortedBy { it.name }.associateBy { it.name }.toMutableMap()
            debug("Found entries $entries")
            val metadataEntry = entries[metadataFileName] ?: throw IllegalStateException("Project file is invalid")
            InputStreamReader(file.getInputStream(metadataEntry), Charsets.UTF_8).use { metadataReader ->
                val metadata = ApplicationContext.get().gson.fromJson(metadataReader, ProjectMetadata::class.java)
                debug("Found metadata $metadata")
                if (metadata.version != ProjectMetadata().version) {
                    if (!(ProjectMetadata().version.compatibleTo.contains(metadata.version))) {
                        warn("Incompatible save version")
                        throw IllegalStateException("Unsupported save version")
                    } else {
                        info("Loading project with older, but compatible save version. Current: ${ProjectMetadata().version.versionName}; Loaded: ${metadata.version.versionName}")
                    }
                }
                val project = Project(path)
                entries.remove(metadataFileName)
                for (entry in entries.values) {
                    InputStreamReader(file.getInputStream(entry), Charsets.UTF_8).use { sheetReader ->
                        val sheet = Sheet.load(sheetReader.readText())
                        debug("Loaded sheet ${sheet.name.value} from ${project.name}")
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
                debug("Trying to save active project to $path")
                ApplicationContext.get().applicationSettings[ApplicationSettings.LATEST_PROJECT_PATH] = path.toString()
                ZipOutputStream(FileOutputStream(path.toFile())).use { zip ->
                    val metadataEntry = ZipEntry(metadataFileName)
                    zip.putNextEntry(metadataEntry)
                    val metadataBytes = ApplicationContext.get().gson.toJson(ProjectMetadata()).toByteArray(charset)
                    zip.write(metadataBytes, 0, metadataBytes.size)
                    zip.closeEntry()
                    var counter = 1
                    for (sheet in project.sheets) {
                        val entryName = "sheet-$counter.json"
                        val sheetEntry = ZipEntry(entryName)
                        debug("Writing entry $entryName to $path")
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
        debug("Creating new project")
        activeProject.set(Project("Unsaved Project"))
        activeProject.get().sheets.add(Sheet(translate("label.unnamed"), GridView<BaseCell>()))
    }

    fun closeProject() {
        debug("Closing project")
        activeProject.set(null)
        ApplicationContext.get().connectionHandler.clear()
    }
}
