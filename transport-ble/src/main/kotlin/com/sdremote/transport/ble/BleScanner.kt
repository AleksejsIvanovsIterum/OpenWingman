package com.sdremote.transport.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult as AndroidScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.sdremote.protocol.types.ProductId
import com.sdremote.transport.ScanResult
import com.sdremote.transport.ScanService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Android implementation of [ScanService] using the platform LE scanner.
 *
 * Filters by [BleConstants.SERVICE_SD] so we don't get every BLE-advertising
 * device in the venue. Caller is responsible for runtime BLE permissions
 * (BLUETOOTH_SCAN on Android 12+, plus location on legacy).
 */
class AndroidBleScanner(context: Context) : ScanService {
    private val appContext = context.applicationContext

    @SuppressLint("MissingPermission")  // permission is the caller's responsibility
    override fun scan(): Flow<ScanResult> = callbackFlow {
        val adapter: BluetoothAdapter = (appContext.getSystemService(Context.BLUETOOTH_SERVICE)
            as BluetoothManager).adapter ?: run { close(); return@callbackFlow }

        val scanner = adapter.bluetoothLeScanner ?: run { close(); return@callbackFlow }

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BleConstants.SERVICE_SD))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: AndroidScanResult) {
                timber.log.Timber.tag("CLink").d(
                    "scan: %s (%s) rssi=%d adv=%s",
                    result.device.name ?: "?",
                    result.device.address,
                    result.rssi,
                    result.scanRecord?.bytes?.joinToString("") { "%02x".format(it) } ?: "?",
                )
                trySend(result.toAppScanResult())
            }
            override fun onBatchScanResults(results: MutableList<AndroidScanResult>) {
                results.forEach { trySend(it.toAppScanResult()) }
            }
            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("BLE scan failed: $errorCode"))
            }
        }

        scanner.startScan(listOf(filter), settings, callback)
        awaitClose { scanner.stopScan(callback) }
    }
}

@SuppressLint("MissingPermission")
private fun AndroidScanResult.toAppScanResult(): ScanResult {
    val advData = scanRecord?.serviceData?.let { map ->
        // Both lowercase and uppercase string forms appear in the wild; the
        // Android API normalizes to ParcelUuid, but we still need to pick our
        // Device Info Service entry.
        map.entries.firstOrNull { it.key.uuid == BleConstants.SERVICE_DEVICE_INFO }?.value
    }
    var product: ProductId? = null
    var fwMajor: Int? = null
    var fwMinor: Int? = null
    var serial: String? = null
    if (advData != null && advData.size >= 3) {
        product = ProductId.fromCodeOrNull(advData[0].toInt() and 0xFF)
        fwMajor = advData[1].toInt() and 0xFF
        fwMinor = advData[2].toInt() and 0xFF
        // Bytes [3..14] hold an ASCII, NUL-terminated serial number.
        val sb = StringBuilder()
        var i = 3
        while (i < advData.size && i < 15 && advData[i] != 0.toByte()) {
            sb.append(advData[i].toInt().toChar()); i++
        }
        serial = sb.toString().ifEmpty { null }
    }
    return ScanResult(
        address = device.address,
        name = device.name,
        rssi = rssi,
        product = product,
        firmwareMajor = fwMajor,
        firmwareMinor = fwMinor,
        serialNumber = serial,
    )
}
