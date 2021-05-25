package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.grid.BaseCell
import com.github.se7_kn8.xcontrolplus.app.util.FileUtil
import com.github.se7_kn8.xcontrolplus.gridview.GridView
import javafx.beans.property.SimpleObjectProperty
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
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
        val charset = Charset.forName("UTF-8")
        FileUtil.openFileChooser(FileUtil.PROJECT_FILE) { path ->

            val file = ZipFile(path.toFile())
            val entries = file.entries().toList().sortedBy { it.name }.associateBy { it.name }.toMutableMap()
            val metadataEntry = entries[metadataFileName] ?: throw IllegalStateException("Project file is invalid")
            InputStreamReader(file.getInputStream(metadataEntry)).use { metadataReader ->
                val metadata = ApplicationContext.get().gson.fromJson(metadataReader, ProjectMetadata::class.java)
                if (metadata.version != ProjectMetadata().version) {
                    throw IllegalStateException("Unsupported save version")
                }
                val project = Project(path.toString())
                entries.remove(metadataFileName)
                for (entry in entries.values) {
                    InputStreamReader(file.getInputStream(entry)).use { sheetReader ->
                        val sheet = Sheet.load(sheetReader.readText())
                        project.sheets.add(sheet)
                    }
                }
                activeProject.set(project)
            }
        }
    }

    fun saveProject() {
        val charset = Charset.forName("UTF-8")
        activeProject.get()?.let { project ->
            FileUtil.saveFileChooser(FileUtil.PROJECT_FILE) { path ->
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
    }

    companion object {
        const val metadataName = "d"
    }

}
