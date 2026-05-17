package com.sdremote.transport.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.sdremote.transport.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.suspend
import timber.log.Timber

/**
 * Transport over Bluetooth LE using the Nordic ble-ktx library.
 *
 * Lifecycle:
 *   1. [NordicBleTransportFactory.connect] returns a started transport.
 *   2. Caller pumps [incoming] into [com.sdremote.domain.Session].
 *   3. [send] chunks at MTU-3 boundaries; Nordic queues writes safely.
 *   4. [close] disconnects the GATT link.
 */
class NordicBleTransport internal constructor(
    private val manager: WingmanBleManager,
    private val scope: CoroutineScope,
) : Transport {

    override val incoming: Flow<ByteArray> = manager.incomingBytes

    override suspend fun send(bytes: ByteArray) {
        val chunkSize = manager.negotiatedMtu - 3
        var offset = 0
        while (offset < bytes.size) {
            val end = (offset + chunkSize).coerceAtMost(bytes.size)
            val slice = bytes.copyOfRange(offset, end)
            manager.writeCLink(slice)
            offset = end
        }
    }

    override suspend fun close() {
        runCatching { manager.disconnect().suspend() }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}

/** Implementation of [com.sdremote.transport.TransportFactory] for Android. */
class NordicBleTransportFactory(private val appContext: Context) :
    com.sdremote.transport.TransportFactory {

    override suspend fun connect(address: String): Transport {
        val adapter = (appContext.getSystemService(Context.BLUETOOTH_SERVICE)
            as android.bluetooth.BluetoothManager).adapter
            ?: error("Bluetooth adapter unavailable")
        val device: BluetoothDevice = adapter.getRemoteDevice(address)

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val manager = WingmanBleManager(appContext, scope)
        manager.connect(device)
            .timeout(10_000)
            .retry(3, 200)
            .useAutoConnect(false)
            .suspend()

        manager.negotiateMtu(BleConstants.DEFAULT_MTU)
        manager.discoverAndSubscribe()

        return NordicBleTransport(manager, scope)
    }
}

/**
 * The Nordic [BleManager] subclass that knows the SD GATT layout and
 * exposes a [MutableSharedFlow] of received bytes.
 */
internal class WingmanBleManager(
    context: Context,
    private val scope: CoroutineScope,
) : BleManager(context) {

    private val _incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val incomingBytes = _incoming.asSharedFlow()

    private var txChar: BluetoothGattCharacteristic? = null
    private var rxChar: BluetoothGattCharacteristic? = null
    var negotiatedMtu: Int = 23
        private set

    override fun getMinLogPriority() = android.util.Log.VERBOSE
    override fun log(priority: Int, message: String) { Timber.log(priority, message) }

    override fun getGattCallback() = object : BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(BleConstants.SERVICE_SD) ?: return false
            txChar = service.getCharacteristic(BleConstants.CHAR_CLINK_IN)
            rxChar = service.getCharacteristic(BleConstants.CHAR_CLINK_OUT)
            return txChar != null && rxChar != null
        }

        override fun initialize() {
            setNotificationCallback(rxChar).with { _, data ->
                data.value?.let { scope.launch { _incoming.emit(it) } }
            }
            enableNotifications(rxChar).enqueue()
        }

        override fun onServicesInvalidated() {
            txChar = null
            rxChar = null
        }
    }

    suspend fun discoverAndSubscribe() {
        // The Nordic library performs initialize() (which enables notifications)
        // automatically once isRequiredServiceSupported returns true — this is
        // a placeholder for future explicit work (e.g. reading firmware rev).
    }

    /** requestMtu is protected, expose it via this internal helper. */
    suspend fun negotiateMtu(mtu: Int) {
        requestMtu(mtu).suspend()
    }

    suspend fun writeCLink(bytes: ByteArray) {
        val ch = txChar ?: error("TX characteristic not yet discovered")
        writeCharacteristic(ch, bytes, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            .suspend()
    }

    fun handleMtuChanged(newMtu: Int) { negotiatedMtu = newMtu }
}
