package com.example.model

enum class TransferType {
    SEND,
    RECEIVE
}

data class TransferRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val fileSize: Long,
    val type: TransferType,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true,
    val localFilePath: String? = null,
    val peerAddress: String = ""
)
