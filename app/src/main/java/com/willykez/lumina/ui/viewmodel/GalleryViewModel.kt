package com.willykez.lumina.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.willykez.lumina.data.MediaItem
import com.willykez.lumina.data.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MediaRepository(app)

    private val _images = MutableStateFlow<List<MediaItem>>(emptyList())
    val images: StateFlow<List<MediaItem>> = _images

    private val _albums = MutableStateFlow<Map<String, List<MediaItem>>>(emptyMap())
    val albums: StateFlow<Map<String, List<MediaItem>>> = _albums

    private val _selected = MutableStateFlow<MediaItem?>(null)
    val selected: StateFlow<MediaItem?> = _selected

    private var loaded = false

    fun loadMedia() {
        if (loaded) return
        loaded = true
        viewModelScope.launch(Dispatchers.IO) {
            val imgs = repo.getAllImages()
            _images.value = imgs
            _albums.value = repo.getAlbums(imgs)
        }
    }

    fun select(item: MediaItem?) {
        _selected.value = item
    }
}
