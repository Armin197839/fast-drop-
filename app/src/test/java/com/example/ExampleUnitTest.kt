package com.example

import com.example.util.FileUtils
import com.example.util.QrCodeGenerator
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun test_formatBytes() {
        assertEquals("0 B", FileUtils.formatBytes(0))
        assertEquals("500 B", FileUtils.formatBytes(500))
        assertEquals("1.00 KB", FileUtils.formatBytes(1024))
        assertEquals("1.50 KB", FileUtils.formatBytes(1536))
        assertEquals("1.00 MB", FileUtils.formatBytes(1024 * 1024))
        assertEquals("2.50 GB", FileUtils.formatBytes((2.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun test_sanitizeFilename() {
        assertEquals("clean_file.txt", FileUtils.sanitizeFilename("clean_file.txt"))
        assertEquals("___secret.txt", FileUtils.sanitizeFilename("../secret.txt"))
        assertEquals("my_video.mp4", FileUtils.sanitizeFilename("my:video.mp4"))
        assertEquals("test_file.png", FileUtils.sanitizeFilename("test/file.png"))
    }

    @Test
    fun test_formatSpeed() {
        assertEquals("51.2 KB/s", FileUtils.formatSpeed(0.05))
        assertEquals("4.82 MB/s", FileUtils.formatSpeed(4.82))
        assertEquals("12.50 MB/s", FileUtils.formatSpeed(12.50))
    }

    @Test
    fun test_qrCodeGeneration() {
        val bitmap = QrCodeGenerator.generateQrBitmap("http://192.168.1.10:8080/download", 256)
        assertNotNull(bitmap)
        assertEquals(256, bitmap?.width)
        assertEquals(256, bitmap?.height)
    }

    @Test
    fun test_nonCollidingFile() {
        val tempDir = File.createTempFile("test_dir", "").apply {
            delete()
            mkdir()
        }
        val file1 = FileUtils.getNonCollidingFile(tempDir, "document.pdf")
        assertEquals("document.pdf", file1.name)
        file1.createNewFile()

        val file2 = FileUtils.getNonCollidingFile(tempDir, "document.pdf")
        assertEquals("document (1).pdf", file2.name)

        tempDir.deleteRecursively()
    }
}

