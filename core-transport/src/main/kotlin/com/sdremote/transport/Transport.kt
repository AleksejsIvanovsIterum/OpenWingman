package com.sdremote.transport

import kotlinx.coroutines.flow.Flow

/**
 * Byte-level transport for CLink frames. Implementations:
 *  - [transport-ble] — real Bluetooth Low Energy via Nordic stack
 *  - [transport-mock] — canned responses for development / tests
 *
 * Contract:
 *  - [incoming] emits ALL bytes the device sends, in order, in arbitrary
 *    chunk sizes. Callers are responsible for re-framing into full CLink
 *    frames (use [com.sdremote.protocol.Frame.parse] over an accumulator).
 *  - [send] writes the provided bytes verbatim. Implementations are free
 *    to chunk into MTU-sized writes.
 *  - [close] terminates the underlying connection. Calling it twice is a
 *    no-op.
 */
interface Transport {
    val incoming: Flow<ByteArray>
    suspend fun send(bytes: ByteArray)
    suspend fun close()
}

/**
 * Discovered BLE device summary, surfaced by [ScanService].
 *
 * Address is the BLE MAC on Android (or the Core Bluetooth UUID on iOS).
 * RSSI is in dBm; -50 ≈ near, -90 ≈ at range limit.
 *
 * The product/firmware/serial fields are decoded from the 0x180A service
 * advertisement payload — see PROTOCOL.md section §2.
 */
data class ScanResult(
    val address: String,
    val name: String?,
    val rssi: Int,
    val product: com.sdremote.protocol.types.ProductId? = null,
    val firmwareMajor: Int? = null,
    val firmwareMinor: Int? = null,
    val serialNumber: String? = null,
) {
    val displayName: String
        get() = serialNumber?.let { "${product?.displayName ?: name ?: "?"} · $it" }
            ?: (name ?: address)
}

/**
 * Scanning is platform-specific. [ScanService] hides the platform behind
 * a coroutines-friendly interface.
 */
interface ScanService {
    /** Hot flow of scan results. Each subscription re-starts a scan; cancelling stops it. */
    fun scan(): Flow<ScanResult>
}

/** Factory for creating a [Transport] given a previously-scanned device. */
interface TransportFactory {
    suspend fun connect(address: String): Transport
}
