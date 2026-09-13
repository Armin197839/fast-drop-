package com.example.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.Locale

object FileUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024.0 && unitIndex < units.size - 1) {
            size /= 1024.0
            unitIndex++
        }
        return if (unitIndex == 0) {
            "${bytes} B"
        } else {
            String.format(Locale.US, "%.2f %s", size, units[unitIndex])
        }
    }

    fun formatSpeed(speedMBps: Double): String {
        return if (speedMBps < 0.1) {
            val kbps = speedMBps * 1024.0
            String.format(Locale.US, "%.1f KB/s", kbps)
        } else {
            String.format(Locale.US, "%.2f MB/s", speedMBps)
        }
    }

    fun sanitizeFilename(name: String): String {
        var clean = name.replace("[/\\\\:*?\"<>|]".toRegex(), "_")
        clean = clean.replace("..", "_")
        clean = clean.trim()
        if (clean.isEmpty() || clean == "." || clean == "..") {
            clean = "FastDrop_file_${System.currentTimeMillis()}"
        }
        return clean
    }

    fun getNonCollidingFile(directory: File, baseFilename: String): File {
        val sanitized = sanitizeFilename(baseFilename)
        var targetFile = File(directory, sanitized)
        if (!targetFile.exists()) {
            return targetFile
        }

        val dotIndex = sanitized.lastIndexOf('.')
        val nameWithoutExt = if (dotIndex > 0) sanitized.substring(0, dotIndex) else sanitized
        val extension = if (dotIndex > 0) sanitized.substring(dotIndex) else ""

        var counter = 1
        while (targetFile.exists()) {
            val candidateName = "$nameWithoutExt ($counter)$extension"
            targetFile = File(directory, candidateName)
            counter++
        }
        return targetFile
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name.isNullOrEmpty()) {
            name = uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
        }
        return sanitizeFilename(name)
    }

    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        var size: Long = -1L
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.SIZE)
                    if (index != -1) {
                        size = it.getLong(index)
                    }
                }
            }
        }
        if (size <= 0) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    size = pfd.statSize
                }
            } catch (_: Exception) {}
        }
        return size
    }

    fun getMimeTypeFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "application/octet-stream"
    }
}
