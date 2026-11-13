package kg.timmitof.core.ui.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Спрашивает разрешение на уведомления (Android 13+) перед действием, которое их показывает.
 * Действие выполняется при любом ответе: без разрешения оно просто пройдёт без уведомления.
 * Разрешение уже есть или Android старше 13 — действие выполняется сразу, без диалога.
 */
@Composable
fun rememberNotificationPermissionRequest(): NotificationPermissionRequest {
    val context = LocalContext.current
    val request = remember(context) { NotificationPermissionRequest(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        request.onResult()
    }
    SideEffect { request.launcher = launcher }
    return request
}

class NotificationPermissionRequest internal constructor(private val context: Context) {

    internal var launcher: ActivityResultLauncher<String>? = null

    private var pending: (() -> Unit)? = null

    fun runWithPermission(action: () -> Unit) {
        val launcher = launcher
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || isGranted() || launcher == null) {
            action()
            return
        }
        pending = action
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    internal fun onResult() {
        pending?.invoke()
        pending = null
    }

    private fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
