package com.github.ai_mind_companion.app.audioPermission

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioPermissionScreen(
    navController : NavController
) {
    val audioPermissionState = rememberPermissionState(permission = android.Manifest.permission.RECORD_AUDIO)

    when (audioPermissionState.status) {
        is PermissionStatus.Granted -> {
            Text("Permission Granted: You can record audio now.")
            LaunchedEffect(Unit) {
                navController.navigate("landingScreen")
            }
        }
        is PermissionStatus.Denied -> {
            val deniedStatus = audioPermissionState.status as PermissionStatus.Denied
            if (deniedStatus.shouldShowRationale) {
                Column {
                    Text("The app requires access to your microphone to record audio.")
                    Button(onClick = { audioPermissionState.launchPermissionRequest() }) {
                        Text("Request Permission")
                    }
                }
            } else {
                Button(onClick = { audioPermissionState.launchPermissionRequest() }) {
                    Text("Request Permission")
                }
            }
        }
    }
}
