package com.sdremote.transport.ble

import java.util.UUID

/**
 * Sound Devices BLE service / characteristic UUIDs — recovered from
 * DeviceConnection.cs (Wingman 5.03).
 */
object BleConstants {
    /** Proprietary Sound Devices service. App filters scans by this UUID. */
    val SERVICE_SD: UUID = UUID.fromString("91a7b1c3-f9d5-4679-9d26-e134067591fb")

    /** TX characteristic — app writes CLink frames here (write w/o response). */
    val CHAR_CLINK_IN: UUID = UUID.fromString("e66f3aa8-e06b-4b7d-a77e-3aa08ebb3f7f")

    /** RX characteristic — device notifies CLink responses here. */
    val CHAR_CLINK_OUT: UUID = UUID.fromString("5049f187-3415-4e2d-bad5-00be79793d27")

    /** Standard SIG Device Information Service — used for advertisement payload. */
    val SERVICE_DEVICE_INFO: UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")

    /** Standard SIG Firmware Revision String characteristic. */
    val CHAR_FIRMWARE_REVISION: UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")

    /** Per the original implementation: split BLE writes at 20 bytes. We
     *  negotiate a higher MTU and split at MTU-3 bytes instead. */
    const val DEFAULT_MTU = 247

    /** Maximum bytes the device accepts in one write after MTU negotiation. */
    const val MAX_WRITE_PAYLOAD = DEFAULT_MTU - 3  // 3 bytes ATT header
}
