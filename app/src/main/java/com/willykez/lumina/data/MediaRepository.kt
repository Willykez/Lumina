package com.willykez.lumina.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

class MediaRepository(private val context: Context) {

    fun getAllImages(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { c ->
            val iId = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val iName = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val iSize = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val iDate = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val iW = c.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val iH = c.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val iBkt = c.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (c.moveToNext()) {
                val id = c.getLong(iId)
                items += MediaItem(
                    id = id,
                    uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                    name = c.getString(iName) ?: "",
                    size = c.getLong(iSize),
                    dateAdded = c.getLong(iDate),
                    width = c.getInt(iW),
                    height = c.getInt(iH),
                    bucketName = c.getString(iBkt) ?: "Unknown"
                )
            }
        }
        return items
    }

    fun getAlbums(items: List<MediaItem>): Map<String, List<MediaItem>> =
        items.groupBy { it.bucketName }
}
