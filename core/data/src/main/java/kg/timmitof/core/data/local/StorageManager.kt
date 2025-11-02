package kg.timmitof.core.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class StorageManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun saveToLocalFiles(fileName: String, inputStream: InputStream): String {
        val destinationFolder = File(context.filesDir, TEMPLATES).apply { mkdir() }
        val destinationFile = File(destinationFolder, fileName).apply { createNewFile() }

        inputStream.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        }

        return destinationFile.absolutePath
    }

    companion object {
        const val TEMPLATES = "templates"
    }
}