package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DynamicAppBackground
import com.example.ui.pages.FileManagerPage
import com.example.ui.pages.HistoryPage
import com.example.ui.pages.HomePage
import com.example.ui.pages.ReceivePage
import com.example.ui.pages.SendPage
import com.example.ui.pages.SettingsPage
import com.example.ui.pages.WebSharePage
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FastDropViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        FastDropApp()
                    }
                }
            }
        }
    }
}

@Composable
fun FastDropApp(viewModel: FastDropViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentBgTheme by viewModel.currentBgTheme.collectAsStateWithLifecycle()
    val wifiIp by viewModel.wifiIp.collectAsStateWithLifecycle()
    val isWifiConnected by viewModel.isWifiConnected.collectAsStateWithLifecycle()
    val selectedFile by viewModel.selectedFile.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()
    val serverProgress by viewModel.serverProgress.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    // Extra features state
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val discoveredPeers by viewModel.discoveredPeers.collectAsStateWithLifecycle()
    val isScanningPeers by viewModel.isScanningPeers.collectAsStateWithLifecycle()
    val customPort by viewModel.customPort.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onFileSelected(uri)
            viewModel.navigateTo(AppScreen.SEND)
            viewModel.startSharing()
        }
    }

    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.navigateTo(AppScreen.HOME)
    }

    DynamicAppBackground(theme = currentBgTheme) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                AppScreen.HOME -> {
                    HomePage(
                        wifiIp = wifiIp,
                        isWifiConnected = isWifiConnected,
                        currentTheme = currentBgTheme,
                        onSelectTheme = { viewModel.setBgTheme(it) },
                        onRefreshNetwork = { viewModel.refreshNetworkInfo() },
                        onNavigate = { dest -> viewModel.navigateTo(dest) }
                    )
                }
                AppScreen.SEND -> {
                    SendPage(
                        selectedFile = selectedFile,
                        isServerRunning = viewModel.server.isRunning,
                        serverUrl = serverUrl,
                        qrBitmap = qrBitmap,
                        serverProgress = serverProgress,
                        onBack = { viewModel.navigateTo(AppScreen.HOME) },
                        onFileSelected = { uri -> viewModel.onFileSelected(uri) },
                        onStartSharing = { viewModel.startSharing() },
                        onStopSharing = { viewModel.stopSharing() }
                    )
                }
                AppScreen.RECEIVE -> {
                    ReceivePage(
                        downloadProgress = downloadProgress,
                        discoveredPeers = discoveredPeers,
                        isScanningPeers = isScanningPeers,
                        onScanPeers = { viewModel.scanNearbyPeers() },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) },
                        onStartDownload = { url -> viewModel.startDownload(url) },
                        onCancelDownload = { viewModel.cancelDownload() },
                        onResetDownload = { viewModel.resetDownload() }
                    )
                }
                AppScreen.FILE_MANAGER -> {
                    FileManagerPage(
                        apps = installedApps,
                        isLoading = isLoadingApps,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { cat -> viewModel.setCategory(cat) },
                        onAppSelected = { app -> viewModel.selectAppForSharing(app) },
                        onPickCustomFile = { filePickerLauncher.launch(arrayOf("*/*")) },
                        onRefreshApps = { viewModel.loadInstalledApps() },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
                AppScreen.HISTORY -> {
                    HistoryPage(
                        historyList = historyList,
                        onClearHistory = { viewModel.clearHistory() },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
                AppScreen.WEB_SHARE -> {
                    WebSharePage(
                        wifiIp = wifiIp,
                        selectedFile = selectedFile,
                        serverUrl = serverUrl,
                        isServerRunning = viewModel.server.isRunning,
                        onStartServer = { viewModel.startSharing() },
                        onPickFile = { filePickerLauncher.launch(arrayOf("*/*")) },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsPage(
                        port = customPort,
                        currentTheme = currentBgTheme,
                        onSelectTheme = { viewModel.setBgTheme(it) },
                        onPortChange = { port -> viewModel.setPort(port) },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
            }
        }
    }
}
