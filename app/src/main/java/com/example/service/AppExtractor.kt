package com.example.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.model.LocalAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppExtractor(private val context: Context) {

    suspend fun getInstalledApps(): List<LocalAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val list = mutableListOf<LocalAppInfo>()

        for (app in apps) {
            // Filter non-system apps or updated system apps
            val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            if (isUserApp && app.packageName != context.packageName) {
                try {
                    val label = pm.getApplicationLabel(app).toString()
                    val pInfo = pm.getPackageInfo(app.packageName, 0)
                    val sourceApk = File(app.sourceDir)
                    val size = if (sourceApk.exists()) sourceApk.length() else 0L

                    list.add(
                        LocalAppInfo(
                            packageName = app.packageName,
                            appName = label,
                            versionName = pInfo.versionName ?: "1.0",
                            sizeBytes = size,
                            apkPath = app.sourceDir
                        )
                    )
                } catch (_: Exception) {}
            }
        }
        list.sortedBy { it.appName.lowercase() }
    }
}
