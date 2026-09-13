package com.example.model

enum class TransferState {
    IDLE,
    PREPARING,
    TRANSFERRING,
    DONE,
    ERROR,
    CANCELLED
}

data class TransferProgress(
    val progress: Float = 0f,
    val speedMBps: Double = 0.0,
    val transferredBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val status: String = "",
    val state: TransferState = TransferState.IDLE,
    val fileName: String = "",
    val savedFilePath: String? = null,
    val errorMessage: String? = null
)
