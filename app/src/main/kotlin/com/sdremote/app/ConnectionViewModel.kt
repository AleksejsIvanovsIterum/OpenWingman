package com.sdremote.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sdremote.domain.Session
import com.sdremote.domain.SessionState
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
 *   - Scan flow (when not connected)
 *   - Active [Session] (once connected + authenticated)
 *
 * Exposes a single [stage] enum that drives `OpenWingmanApp` between
 * "scan" and "connected" surfaces.
 */
class ConnectionViewModel(app: Application) : AndroidViewModel(app) {

    private val scanner = AndroidBleScanner(app)
    private val factory = NordicBleTransportFactory(app)

    enum class Stage { Scanning, Connecting, Connected, Disconnected }

    private val _stage = MutableStateFlow(Stage.Scanning)
    val stage: StateFlow<Stage> = _stage.asStateFlow()

    private val _devices = MutableStateFlow<List<ScanResult>>(emptyList())
    val devices: StateFlow<List<ScanResult>> = _devices.asStateFlow()

    var session: Session? = null
        private set

    private var scanJob: Job? = null

    init { startScan() }

    fun startScan() {
        scanJob?.cancel()
        _stage.value = Stage.Scanning
        _devices.value = emptyList()
        scanJob = viewModelScope.launch {
            runCatching {
                scanner.scan().collect { result ->
                    _devices.update { current ->
                        // dedupe by address; keep the freshest RSSI/payload.
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
                session = s
                _stage.value = Stage.Connected
            }.onFailure {
                _stage.value = Stage.Disconnected
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            session?.stop()
            session = null
            _stage.value = Stage.Disconnected
        }
    }
}
