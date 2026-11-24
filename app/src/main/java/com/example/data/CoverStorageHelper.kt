package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object CoverStorageHelper {
    suspend fun copyCoverToAppStorage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
            val outFile = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            "file://${outFile.absolutePath}"
        } catch (_: Exception) {
            null
        }
    }
}
