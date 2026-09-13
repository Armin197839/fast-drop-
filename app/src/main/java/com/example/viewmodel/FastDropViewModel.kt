package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BackgroundTheme
import com.example.model.FileCategory
import com.example.model.LocalAppInfo
import com.example.model.TransferProgress
import com.example.model.TransferRecord
import com.example.model.TransferState
import com.example.model.TransferType
import com.example.service.AppExtractor
import com.example.service.DiscoveredPeer
import com.example.service.FastDropDownloader
import com.example.service.FastDropServer
import com.example.service.HistoryManager
import com.example.service.NetworkService
import com.example.service.PeerDiscoveryService
import com.example.util.FileUtils
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    HOME,
    SEND,
    RECEIVE,
    FILE_MANAGER,
    HISTORY,
    WEB_SHARE,
    SETTINGS
}

data class SelectedFileInfo(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String
)

class FastDropViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val prefs = context.getSharedPreferences("fastdrop_prefs", Context.MODE_PRIVATE)

    val networkService = NetworkService(context)
    val server = FastDropServer(context)
    val downloader = FastDropDownloader(context)
    val historyManager = HistoryManager(context)
    private val appExtractor = AppExtractor(context)
    private val peerDiscoveryService = PeerDiscoveryService()

    private val _currentBgTheme = MutableStateFlow(
        run {
            val saved = prefs.getString("bg_theme", BackgroundTheme.AURORA_BLUE.id)
            BackgroundTheme.values().find { it.id == saved } ?: BackgroundTheme.AURORA_BLUE
        }
    )
    val currentBgTheme: StateFlow<BackgroundTheme> = _currentBgTheme.asStateFlow()

    fun setBgTheme(theme: BackgroundTheme) {
        _currentBgTheme.value = theme
        prefs.edit().putString("bg_theme", theme.id).apply()
    }

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _wifiIp = MutableStateFlow<String?>(null)
    val wifiIp: StateFlow<String?> = _wifiIp.asStateFlow()

    private val _isWifiConnected = MutableStateFlow(false)
    val isWifiConnected: StateFlow<Boolean> = _isWifiConnected.asStateFlow()

    private val _selectedFile = MutableStateFlow<SelectedFileInfo?>(null)
    val selectedFile: StateFlow<SelectedFileInfo?> = _selectedFile.asStateFlow()

    private val _serverUrl = MutableStateFlow<String?>(null)
    val serverUrl: StateFlow<String?> = _serverUrl.asStateFlow()

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    // Additional Features
    private val _historyList = MutableStateFlow<List<TransferRecord>>(emptyList())
    val historyList: StateFlow<List<TransferRecord>> = _historyList.asStateFlow()

    private val _installedApps = MutableStateFlow<List<LocalAppInfo>>(emptyList())
    val installedApps: StateFlow<List<LocalAppInfo>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    val selectedCategory: StateFlow<FileCategory> = _selectedCategory.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<DiscoveredPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    private val _isScanningPeers = MutableStateFlow(false)
    val isScanningPeers: StateFlow<Boolean> = _isScanningPeers.asStateFlow()

    // Settings
    private val _customPort = MutableStateFlow(8080)
    val customPort: StateFlow<Int> = _customPort.asStateFlow()

    val serverProgress: StateFlow<TransferProgress> = server.serverState
    val downloadProgress: StateFlow<TransferProgress> = downloader.downloadState

    init {
        refreshNetworkInfo()
        loadHistory()
        observeTransfers()
    }

    private fun observeTransfers() {
        viewModelScope.launch {
            serverProgress.collectLatest { progress ->
                if (progress.state == TransferState.DONE) {
                    val file = _selectedFile.value
                    if (file != null) {
                        historyManager.addRecord(
                            TransferRecord(
                                fileName = file.name,
                                fileSize = file.size,
                                type = TransferType.SEND,
                                isSuccess = true
                            )
                        )
                        loadHistory()
                    }
                }
            }
        }

        viewModelScope.launch {
            downloadProgress.collectLatest { progress ->
                if (progress.state == TransferState.DONE) {
                    historyManager.addRecord(
                        TransferRecord(
                            fileName = progress.fileName,
                            fileSize = progress.totalBytes,
                            type = TransferType.RECEIVE,
                            isSuccess = true,
                            localFilePath = progress.savedFilePath
                        )
                    )
                    loadHistory()
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        refreshNetworkInfo()
        if (screen == AppScreen.FILE_MANAGER && _installedApps.value.isEmpty()) {
            loadInstalledApps()
        }
        if (screen == AppScreen.HISTORY) {
            loadHistory()
        }
    }

    fun setCategory(category: FileCategory) {
        _selectedCategory.value = category
    }

    fun loadHistory() {
        _historyList.value = historyManager.getRecords()
    }

    fun clearHistory() {
        historyManager.clearHistory()
        _historyList.value = emptyList()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            _installedApps.value = appExtractor.getInstalledApps()
            _isLoadingApps.value = false
        }
    }

    fun selectAppForSharing(app: LocalAppInfo) {
        val apkFile = File(app.apkPath)
        if (apkFile.exists()) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                _selectedFile.value = SelectedFileInfo(
                    uri = uri,
                    name = "${app.appName}.apk",
                    size = app.sizeBytes,
                    mimeType = "application/vnd.android.package-archive"
                )
                navigateTo(AppScreen.SEND)
                startSharing()
            } catch (_: Exception) {
                // fallback to direct uri if needed
                val directUri = Uri.fromFile(apkFile)
                _selectedFile.value = SelectedFileInfo(
                    uri = directUri,
                    name = "${app.appName}.apk",
                    size = app.sizeBytes,
                    mimeType = "application/vnd.android.package-archive"
                )
                navigateTo(AppScreen.SEND)
                startSharing()
            }
        }
    }

    fun scanNearbyPeers() {
        val ip = _wifiIp.value ?: return
        viewModelScope.launch {
            _isScanningPeers.value = true
            _discoveredPeers.value = peerDiscoveryService.scanLocalSubnet(ip, _customPort.value)
            _isScanningPeers.value = false
        }
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val ip = networkService.getWifiIp()
            val connected = networkService.isConnectedToWifi()
            _wifiIp.value = ip
            _isWifiConnected.value = connected
        }
    }

    fun onFileSelected(uri: Uri) {
        val name = FileUtils.getFileNameFromUri(context, uri)
        val size = FileUtils.getFileSizeFromUri(context, uri)
        val mime = FileUtils.getMimeTypeFromUri(context, uri)

        _selectedFile.value = SelectedFileInfo(
            uri = uri,
            name = name,
            size = size,
            mimeType = mime
        )

        if (server.isRunning) {
            stopSharing()
        }
    }

    fun startSharing() {
        val file = _selectedFile.value ?: return
        refreshNetworkInfo()

        val ip = _wifiIp.value ?: networkService.getWifiIp() ?: "127.0.0.1"
        val port = server.startServer(
            uri = file.uri,
            fileName = file.name,
            fileSize = file.size,
            mimeType = file.mimeType,
            initialPort = _customPort.value
        )

        if (port > 0) {
            val url = "http://$ip:$port/download"
            _serverUrl.value = url
            _qrBitmap.value = QrCodeGenerator.generateQrBitmap(url, 512)
        }
    }

    fun stopSharing() {
        server.stopServer()
        _serverUrl.value = null
        _qrBitmap.value = null
    }

    fun startDownload(url: String) {
        downloader.startDownload(url)
    }

    fun cancelDownload() {
        downloader.cancelDownload()
    }

    fun resetDownload() {
        downloader.resetState()
    }

    fun setPort(port: Int) {
        _customPort.value = port
    }

    override fun onCleared() {
        super.onCleared()
        server.stopServer()
        downloader.cancelDownload()
    }
}
