package com.example.service

import android.content.Context
import android.net.Uri
import com.example.model.TransferProgress
import com.example.model.TransferState
import com.example.util.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class FastDropServer(private val context: Context) {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val serverScope = CoroutineScope(Dispatchers.IO)

    private val _serverState = MutableStateFlow<TransferProgress>(TransferProgress())
    val serverState: StateFlow<TransferProgress> = _serverState.asStateFlow()

    private var currentFileUri: Uri? = null
    private var currentFileName: String = ""
    private var currentFileSize: Long = 0L
    private var currentMimeType: String = "application/octet-stream"

    var boundPort: Int = 8080
        private set

    val isRunning: Boolean
        get() = serverSocket != null && !(serverSocket?.isClosed ?: true)

    fun startServer(uri: Uri, fileName: String, fileSize: Long, mimeType: String, initialPort: Int = 8080): Int {
        stopServer()

        currentFileUri = uri
        currentFileName = fileName
        currentFileSize = fileSize
        currentMimeType = mimeType

        var port = initialPort
        var created = false
        var attempts = 0

        while (!created && attempts < 10) {
            try {
                val socket = ServerSocket()
                socket.reuseAddress = true
                socket.bind(InetSocketAddress("0.0.0.0", port))
                serverSocket = socket
                boundPort = port
                created = true
            } catch (_: Exception) {
                port++
                attempts++
            }
        }

        if (!created) {
            _serverState.value = TransferProgress(
                state = TransferState.ERROR,
                status = "خطا در راه‌اندازی سرور (پورت‌های 8080-8090 در دسترس نیستند)",
                errorMessage = "خطا در اتصال به پورت سرور"
            )
            return -1
        }

        _serverState.value = TransferProgress(
            state = TransferState.IDLE,
            status = "سرور فعال شد - آماده ارسال",
            fileName = fileName,
            totalBytes = fileSize
        )

        serverJob = serverScope.launch {
            listenForClients()
        }

        return boundPort
    }

    private suspend fun listenForClients() {
        val socket = serverSocket ?: return
        while (socket.isBound && !socket.isClosed && serverScope.isActive) {
            try {
                val clientSocket = withContext(Dispatchers.IO) {
                    socket.accept()
                }
                serverScope.launch {
                    handleClient(clientSocket)
                }
            } catch (_: Exception) {
                break
            }
        }
    }

    private suspend fun handleClient(client: Socket) {
        withContext(Dispatchers.IO) {
            try {
                client.soTimeout = 30000
                val reader = BufferedReader(InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))
                val output = client.getOutputStream()

                val requestLine = reader.readLine() ?: run {
                    client.close()
                    return@withContext
                }

                val parts = requestLine.split(" ")
                if (parts.size < 2) {
                    send400(output)
                    client.close()
                    return@withContext
                }

                val method = parts[0]
                val path = parts[1]

                // Consume remaining headers
                var headerLine: String? = reader.readLine()
                while (!headerLine.isNullOrEmpty()) {
                    headerLine = reader.readLine()
                }

                if (method != "GET") {
                    send405(output)
                    client.close()
                    return@withContext
                }

                when {
                    path == "/download" || path.startsWith("/download?") -> {
                        streamFileToClient(output)
                    }
                    path == "/info" -> {
                        sendInfo(output)
                    }
                    path == "/" -> {
                        sendLandingPage(output)
                    }
                    else -> {
                        send404(output)
                    }
                }
            } catch (_: Exception) {
                // Client connection reset or disconnect
            } finally {
                try {
                    client.close()
                } catch (_: Exception) {}
            }
        }
    }

    private fun streamFileToClient(output: OutputStream) {
        val uri = currentFileUri ?: run {
            send404(output)
            return
        }

        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                send404(output)
                return
            }

            val encodedFileName = URLEncoder.encode(currentFileName, "UTF-8").replace("+", "%20")
            val headers = buildString {
                append("HTTP/1.1 200 OK\r\n")
                append("Content-Type: $currentMimeType\r\n")
                if (currentFileSize > 0) {
                    append("Content-Length: $currentFileSize\r\n")
                }
                append("Content-Disposition: attachment; filename=\"$currentFileName\"; filename*=UTF-8''$encodedFileName\r\n")
                append("Accept-Ranges: none\r\n")
                append("Connection: close\r\n")
                append("Access-Control-Allow-Origin: *\r\n")
                append("\r\n")
            }

            output.write(headers.toByteArray(StandardCharsets.UTF_8))
            output.flush()

            _serverState.value = TransferProgress(
                state = TransferState.TRANSFERRING,
                status = "در حال ارسال فایل...",
                fileName = currentFileName,
                totalBytes = currentFileSize,
                transferredBytes = 0L,
                progress = 0f
            )

            val buffer = ByteArray(64 * 1024)
            var bytesTransferred = 0L
            val startTime = System.currentTimeMillis()
            var lastUpdateTime = startTime

            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                bytesTransferred += read

                val now = System.currentTimeMillis()
                if (now - lastUpdateTime > 200 || bytesTransferred == currentFileSize) {
                    val elapsedSec = (now - startTime) / 1000.0
                    val speedMBps = if (elapsedSec > 0.05) {
                        (bytesTransferred / (1024.0 * 1024.0)) / elapsedSec
                    } else 0.0

                    val ratio = if (currentFileSize > 0) {
                        (bytesTransferred.toFloat() / currentFileSize.toFloat()).coerceIn(0f, 1f)
                    } else -1f

                    _serverState.value = TransferProgress(
                        state = TransferState.TRANSFERRING,
                        status = "در حال ارسال: ${FileUtils.formatBytes(bytesTransferred)} / ${FileUtils.formatBytes(currentFileSize)}",
                        fileName = currentFileName,
                        totalBytes = currentFileSize,
                        transferredBytes = bytesTransferred,
                        progress = ratio,
                        speedMBps = speedMBps
                    )
                    lastUpdateTime = now
                }
            }
            output.flush()

            _serverState.value = TransferProgress(
                state = TransferState.DONE,
                status = "ارسال فایل با موفقیت پایان یافت!",
                fileName = currentFileName,
                totalBytes = currentFileSize,
                transferredBytes = bytesTransferred,
                progress = 1f
            )
        } catch (e: Exception) {
            _serverState.value = TransferProgress(
                state = TransferState.ERROR,
                status = "انتقال قطع شد یا خطا رخ داد: ${e.message ?: "قطع ارتباط"}",
                fileName = currentFileName,
                totalBytes = currentFileSize,
                errorMessage = e.message
            )
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
        }
    }

    private fun sendInfo(output: OutputStream) {
        val json = """
            {
              "name": "${currentFileName.replace("\"", "\\\"")}",
              "size": $currentFileSize,
              "mimeType": "$currentMimeType"
            }
        """.trimIndent()

        val response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n" +
                "Content-Length: ${json.toByteArray(StandardCharsets.UTF_8).size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n" + json

        output.write(response.toByteArray(StandardCharsets.UTF_8))
        output.flush()
    }

    private fun sendLandingPage(output: OutputStream) {
        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="fa">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>FastDrop ⚡</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #F5F7FB; color: #1A1A1A; display: flex; align-items: center; justify-content: center; min-height: 100vh; margin: 0; padding: 20px; }
                    .card { background: white; border-radius: 20px; padding: 32px; box-shadow: 0 10px 30px rgba(0,0,0,0.08); max-width: 420px; width: 100%; text-align: center; }
                    .icon { font-size: 48px; margin-bottom: 12px; }
                    h1 { margin: 0 0 8px; color: #2962FF; font-size: 24px; }
                    .desc { color: #6B7280; margin-bottom: 24px; font-size: 14px; }
                    .file-box { background: #EDF2F9; border-radius: 12px; padding: 16px; margin-bottom: 24px; text-align: right; }
                    .file-name { font-weight: bold; word-break: break-all; margin-bottom: 4px; }
                    .file-size { color: #6B7280; font-size: 13px; }
                    .btn { display: block; background: #00B894; color: white; text-decoration: none; padding: 14px 20px; border-radius: 12px; font-weight: bold; font-size: 16px; transition: 0.2s; }
                    .btn:hover { background: #00A383; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">⚡</div>
                    <h1>FastDrop</h1>
                    <div class="desc">فایل آماده دریافت در شبکه محلی است</div>
                    <div class="file-box">
                        <div class="file-name">${currentFileName}</div>
                        <div class="file-size">${FileUtils.formatBytes(currentFileSize)}</div>
                    </div>
                    <a href="/download" class="btn">📥 دانلود مستقیم فایل</a>
                </div>
            </body>
            </html>
        """.trimIndent()

        val bytes = html.toByteArray(StandardCharsets.UTF_8)
        val response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"

        output.write(response.toByteArray(StandardCharsets.UTF_8))
        output.write(bytes)
        output.flush()
    }

    private fun send404(output: OutputStream) {
        val msg = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
        output.write(msg.toByteArray(StandardCharsets.UTF_8))
        output.flush()
    }

    private fun send400(output: OutputStream) {
        val msg = "HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
        output.write(msg.toByteArray(StandardCharsets.UTF_8))
        output.flush()
    }

    private fun send405(output: OutputStream) {
        val msg = "HTTP/1.1 405 Method Not Allowed\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
        output.write(msg.toByteArray(StandardCharsets.UTF_8))
        output.flush()
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null

        serverJob?.cancel()
        serverJob = null

        _serverState.value = TransferProgress(state = TransferState.IDLE, status = "سرور متوقف شد")
    }
}
