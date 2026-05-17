package com.sdremote.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Permissions required to scan and connect to BLE devices. */
val requiredBlePermissions: Array<String> = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
    )
    else -> arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,  // pre-12 quirk: BLE scan = location
    )
}

/** Returns true when every required BLE permission is granted. */
fun Context.haveBlePermissions(): Boolean = requiredBlePermissions.all { perm ->
    ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
}

/**
 * Composable that auto-requests required BLE permissions on first composition
 * and updates [hasPermissionState] accordingly.
 *
 * Usage:
 * ```
 *   var hasPerm by remember { mutableStateOf(context.haveBlePermissions()) }
 *   PermissionGate(onGranted = { hasPerm = true }) { trigger -> /* render button */ }
 * ```
 */
@Composable
fun rememberBlePermissionGate(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        onResult(results.values.all { it })
    }
    return remember(launcher) { { launcher.launch(requiredBlePermissions) } }
}

/**
 * Runs [onGranted] / [onDenied] callbacks once on first composition based on
 * current permission state. Convenient for "I'm starting the app, do I have
 * the permissions I need" checks.
 */
@Composable
fun OneShotPermissionCheck(
    onResult: (granted: Boolean) -> Unit,
) {
    val context = LocalContext.current
    var ran by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!ran) {
            ran = true
            onResult(context.haveBlePermissions())
        }
    }
}
