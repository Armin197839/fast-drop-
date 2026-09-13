package com.example.service

import android.content.Context
import android.os.Environment
import com.example.model.TransferProgress
import com.example.model.TransferState
import com.example.util.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

class FastDropDownloader(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private var activeCall: Call? = null
    private var downloadJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _downloadState = MutableStateFlow<TransferProgress>(TransferProgress())
    val downloadState: StateFlow<TransferProgress> = _downloadState.asStateFlow()

    fun startDownload(rawUrl: String) {
        cancelDownload()

        var formattedUrl = rawUrl.trim()
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "http://$formattedUrl"
        }

        val parsedUri = try {
            java.net.URI(formattedUrl)
        } catch (_: Exception) {
            _downloadState.value = TransferProgress(
                state = TransferState.ERROR,
                status = "آدرس URL نامعتبر است",
                errorMessage = "فرمت آدرس صحیح نیست"
            )
            return
        }

        if (parsedUri.host.isNullOrBlank()) {
            _downloadState.value = TransferProgress(
                state = TransferState.ERROR,
                status = "آدرس نامعتبر است (میزبان یافت نشد)",
                errorMessage = "IP یا نام میزبان مشخص نشده است"
            )
            return
        }

        _downloadState.value = TransferProgress(
            state = TransferState.PREPARING,
            status = "در حال اتصال به فرستنده..."
        )

        downloadJob = scope.launch {
            executeDownload(formattedUrl)
        }
    }

    private suspend fun executeDownload(url: String) {
        withContext(Dispatchers.IO) {
            var tempPartFile: File? = null
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "FastDrop-Android/1.0")
                    .build()

                val call = client.newCall(request)
                activeCall = call

                val response = call.execute()
                if (!response.isSuccessful) {
                    val code = response.code
                    _downloadState.value = TransferProgress(
                        state = TransferState.ERROR,
                        status = "خطای سرور: کد وضعیت $code",
                        errorMessage = "پاسخ سرور ناموفق بود ($code)"
                    )
                    return@withContext
                }

                val body = response.body ?: run {
                    _downloadState.value = TransferProgress(
                        state = TransferState.ERROR,
                        status = "پاسخ خالی از فرستنده دریافت شد",
                        errorMessage = "بدنه پاسخ خالی است"
                    )
                    return@withContext
                }

                val contentLength = body.contentLength()

                // Resolve filename
                var resolvedName: String? = null
                val disposition = response.header("Content-Disposition")
                if (!disposition.isNullOrEmpty()) {
                    val matchStar = Regex("""filename\*=UTF-8''([^;]+)""").find(disposition)
                    if (matchStar != null) {
                        try {
                            resolvedName = URLDecoder.decode(matchStar.groupValues[1], "UTF-8")
                        } catch (_: Exception) {}
                    }
                    if (resolvedName.isNullOrEmpty()) {
                        val matchNormal = Regex("""filename=["']?([^"';]+)["']?""").find(disposition)
                        if (matchNormal != null) {
                            resolvedName = matchNormal.groupValues[1]
                        }
                    }
                }

                if (resolvedName.isNullOrEmpty()) {
                    val pathSegments = request.url.pathSegments
                    if (pathSegments.isNotEmpty() && pathSegments.last().isNotBlank() && pathSegments.last() != "download") {
                        resolvedName = pathSegments.last()
                    }
                }

                if (resolvedName.isNullOrEmpty()) {
                    resolvedName = "FastDrop_file_${System.currentTimeMillis()}"
                }

                resolvedName = FileUtils.sanitizeFilename(resolvedName)

                // Determine target directory
                val targetDir = getTargetDirectory()
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                val finalFile = FileUtils.getNonCollidingFile(targetDir, resolvedName)
                tempPartFile = File(targetDir, "${finalFile.name}.part")
                if (tempPartFile.exists()) {
                    tempPartFile.delete()
                }

                outputStream = FileOutputStream(tempPartFile)
                inputStream = body.byteStream()

                val buffer = ByteArray(64 * 1024)
                var bytesReadTotal = 0L
                val startTime = System.currentTimeMillis()
                var lastUpdateTime = startTime

                _downloadState.value = TransferProgress(
                    state = TransferState.TRANSFERRING,
                    status = "در حال دانلود...",
                    fileName = finalFile.name,
                    totalBytes = contentLength,
                    transferredBytes = 0L,
                    progress = 0f
                )

                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                    bytesReadTotal += read

                    val now = System.currentTimeMillis()
                    if (now - lastUpdateTime > 200 || (contentLength > 0 && bytesReadTotal == contentLength)) {
                        val elapsedSec = (now - startTime) / 1000.0
                        val speedMBps = if (elapsedSec > 0.05) {
                            (bytesReadTotal / (1024.0 * 1024.0)) / elapsedSec
                        } else 0.0

                        val ratio = if (contentLength > 0) {
                            (bytesReadTotal.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                        } else -1f

                        _downloadState.value = TransferProgress(
                            state = TransferState.TRANSFERRING,
                            status = "در حال دریافت: ${FileUtils.formatBytes(bytesReadTotal)} / ${if (contentLength > 0) FileUtils.formatBytes(contentLength) else "نامشخص"}",
                            fileName = finalFile.name,
                            totalBytes = contentLength,
                            transferredBytes = bytesReadTotal,
                            progress = ratio,
                            speedMBps = speedMBps
                        )
                        lastUpdateTime = now
                    }
                }

                outputStream.flush()
                outputStream.close()
                outputStream = null

                inputStream.close()
                inputStream = null

                // Rename temporary .part file to final file
                val renamed = tempPartFile.renameTo(finalFile)
                val destination = if (renamed) finalFile else tempPartFile

                _downloadState.value = TransferProgress(
                    state = TransferState.DONE,
                    status = "فایل با موفقیت ذخیره شد!",
                    fileName = destination.name,
                    totalBytes = bytesReadTotal,
                    transferredBytes = bytesReadTotal,
                    progress = 1f,
                    savedFilePath = destination.absolutePath
                )
            } catch (e: Exception) {
                // If cancelled or failed, cleanup incomplete .part file
                try {
                    outputStream?.close()
                } catch (_: Exception) {}
                try {
                    inputStream?.close()
                } catch (_: Exception) {}

                if (tempPartFile?.exists() == true) {
                    tempPartFile.delete()
                }

                if (activeCall?.isCanceled() == true) {
                    _downloadState.value = TransferProgress(
                        state = TransferState.CANCELLED,
                        status = "انتقال توسط کاربر لغو شد."
                    )
                } else {
                    val userMsg = when {
                        e is java.net.ConnectException -> "ارتباط با فرستنده برقرار نشد. اتصال Wi-Fi هر دو دستگاه را بررسی کنید."
                        e is java.net.SocketTimeoutException -> "زمان اتصال به پایان رسید. سرعت یا اتصال شبکه را بررسی کنید."
                        e is java.net.UnknownHostException -> "آدرس یا IP وارد شده یافت نشد."
                        else -> "خطا در دانلود فایل: ${e.localizedMessage ?: "قطع ارتباط شبکه"}"
                    }
                    _downloadState.value = TransferProgress(
                        state = TransferState.ERROR,
                        status = userMsg,
                        errorMessage = e.message
                    )
                }
            } finally {
                activeCall = null
            }
        }
    }

    private fun getTargetDirectory(): File {
        // Try public Download/FastDrop
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fastDropPublic = File(publicDownloads, "FastDrop")
        return try {
            if (fastDropPublic.exists() || fastDropPublic.mkdirs()) {
                fastDropPublic
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            }
        } catch (_: Exception) {
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        }
    }

    fun cancelDownload() {
        try {
            activeCall?.cancel()
        } catch (_: Exception) {}
        activeCall = null

        downloadJob?.cancel()
        downloadJob = null

        if (_downloadState.value.state == TransferState.TRANSFERRING || _downloadState.value.state == TransferState.PREPARING) {
            _downloadState.value = TransferProgress(
                state = TransferState.CANCELLED,
                status = "انتقال لغو شد."
            )
        }
    }

    fun resetState() {
        cancelDownload()
        _downloadState.value = TransferProgress(state = TransferState.IDLE)
    }
}
