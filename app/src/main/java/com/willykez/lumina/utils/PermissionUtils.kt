package com.willykez.lumina.utils

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestMediaPermission(onGranted: () -> Unit) {
    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        listOf(Manifest.permission.READ_MEDIA_IMAGES)
    else
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    val state = rememberMultiplePermissionsState(perms)

    LaunchedEffect(state.allPermissionsGranted) {
        if (state.allPermissionsGranted) {
            onGranted()
        } else {
            state.launchMultiplePermissionRequest()
        }
    }
}
