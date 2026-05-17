package com.sdremote.protocol.auth

import java.security.MessageDigest

/**
 * CLink dongle authentication.
 *
 * On connect, the device emits an `Authenticate` packet containing
 * `[0x04][challenge_len][challenge_bytes...]`. The client responds with
 * `[0x01][0x14][SHA1(challenge || AUTH_KEY)]` — a 20-byte hash. The device
 * then either ACKs (success) or closes the connection.
 *
 * Note: this is NOT a cryptographic secret. AUTH_KEY is a hardcoded
 * constant Sound Devices uses as a "trusted client" gate. We extracted it
 * via clean-room decompilation of the publicly distributed APK; the value
 * itself is a fact (uncopyrightable). See REVERSE-ENGINEERING.md.
 */
object Authentication {
    /** The 8-byte gate key, identical in every Wingman release we've seen. */
    val AUTH_KEY: ByteArray = byteArrayOf(
        0x38, 0x6A.toByte(), 0x9B.toByte(), 0x16,
        0x07, 0x68, 0x3A, 0x34,
    )

    /** Size of the SHA-1 response in bytes. */
    const val RESPONSE_SIZE = 20

    /** Compute the response hash for a given challenge. */
    fun computeResponse(challenge: ByteArray): ByteArray {
        val buf = ByteArray(challenge.size + AUTH_KEY.size).also {
            challenge.copyInto(it)
            AUTH_KEY.copyInto(it, challenge.size)
        }
        return MessageDigest.getInstance("SHA-1").digest(buf).also {
            check(it.size == RESPONSE_SIZE) { "SHA-1 output unexpectedly ${it.size} bytes" }
        }
    }

    /**
     * Hash a user-entered remote password — single SHA-1 of UTF-8 bytes, no
     * salt. (We can do nothing about this on the client side; the device
     * firmware does the same hash, so altering it would break compatibility.)
     */
    fun hashRemotePassword(password: String): ByteArray =
        MessageDigest.getInstance("SHA-1")
            .digest(password.toByteArray(Charsets.UTF_8))
}
