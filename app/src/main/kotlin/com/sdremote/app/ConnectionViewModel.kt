package com.sdremote.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sdremote.domain.Session
import com.sdremote.transport.ScanResult
import com.sdremote.transport.ble.AndroidBleScanner
import com.sdremote.transport.ble.NordicBleTransportFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds whole-app connection state:
 *   - permission gate
 *   - scan flow (when not connected)
 *   - active [Session] (once connected + authenticated)
 *
 * Exposes a single [stage] enum that drives `OpenWingmanApp` between
 * scan / connecting / connected surfaces.
 */
class ConnectionViewModel(app: Application) : AndroidViewModel(app) {

    private val scanner = AndroidBleScanner(app)
    private val factory = NordicBleTransportFactory(app)

    enum class Stage { PermissionMissing, Scanning, Connecting, Connected, Disconnected }

    private val _stage = MutableStateFlow(Stage.PermissionMissing)
    val stage: StateFlow<Stage> = _stage.asStateFlow()

    private val _devices = MutableStateFlow<List<ScanResult>>(emptyList())
    val devices: StateFlow<List<ScanResult>> = _devices.asStateFlow()

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    private var scanJob: Job? = null

    /** Called from the UI once runtime BLE permissions are confirmed. */
    fun onPermissionsGranted() {
        if (_stage.value == Stage.PermissionMissing) startScan()
    }

    /** Called by the UI when the user denies the prompt. */
    fun onPermissionsDenied() {
        _stage.value = Stage.PermissionMissing
    }

    fun startScan() {
        scanJob?.cancel()
        _stage.value = Stage.Scanning
        _devices.value = emptyList()
        scanJob = viewModelScope.launch {
            runCatching {
                scanner.scan().collect { result ->
                    _devices.update { current ->
                        (current.filterNot { it.address == result.address } + result)
                            .sortedByDescending { it.rssi }
                    }
                }
            }
        }
    }

    fun connect(target: ScanResult) {
        scanJob?.cancel()
        _stage.value = Stage.Connecting
        viewModelScope.launch {
            runCatching {
                val transport = factory.connect(target.address)
                val s = Session(transport, viewModelScope)
                s.start()
                _session.value = s
                _stage.value = Stage.Connected
            }.onFailure {
                _stage.value = Stage.Disconnected
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _session.value?.stop()
            _session.value = null
            _stage.value = Stage.Disconnected
        }
    }
}
