package com.willykez.lumina.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val size: Long,
    val dateAdded: Long,
    val width: Int = 0,
    val height: Int = 0,
    val bucketName: String = "Unknown"
)
