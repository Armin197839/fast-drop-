package com.example.model

enum class FileCategory(val title: String) {
    ALL("همه"),
    APPS("برنامه‌ها"),
    IMAGES("تصاویر"),
    VIDEOS("ویدیوها"),
    MUSIC("موسیقی"),
    DOCS("اسناد")
}

data class LocalAppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val sizeBytes: Long,
    val apkPath: String
)
