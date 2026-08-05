package com.inspiredandroid.betabase

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.inspiredandroid.betabase.data.ReminderPermissionBridge
import com.inspiredandroid.betabase.data.ReminderPermissionRequestSession

class MainActivity : ComponentActivity() {

    private val permissionSession = ReminderPermissionRequestSession()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionSession.complete(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)

        ReminderPermissionBridge.requestPermission = {
            if (Build.VERSION.SDK_INT < 33) {
                true
            } else {
                permissionSession.await {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        setContent {
            BetabaseApp()
        }
    }

    override fun onDestroy() {
        ReminderPermissionBridge.requestPermission = null
        super.onDestroy()
    }
}
