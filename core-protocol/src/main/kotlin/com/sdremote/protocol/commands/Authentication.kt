package com.sdremote.protocol.commands

import com.sdremote.protocol.Codec
import com.sdremote.protocol.CommandId
import com.sdremote.protocol.Frame
import com.sdremote.protocol.auth.Authentication

/**
 * Initial Authenticate request — empty payload. Device responds with a
 * challenge inside a DATA frame.
 */
data object AuthenticateInit : CLinkRequest {
    override val id = CommandId.Authenticate
}

/** Reply to a challenge — `[0x01][0x14][hash20]`. */
data class AuthenticateResponse(val responseHash: ByteArray) : CLinkRequest {
    init {
        require(responseHash.size == Authentication.RESPONSE_SIZE) {
            "Auth response must be ${Authentication.RESPONSE_SIZE} bytes, got ${responseHash.size}"
        }
    }
    override val id = CommandId.Authenticate

    override fun serializePayload(): ByteArray = Codec.Writer(2 + Authentication.RESPONSE_SIZE)
        .u8(0x01)
        .u8(Authentication.RESPONSE_SIZE)
        .raw(responseHash)
        .toByteArray()

    override fun equals(other: Any?): Boolean =
        other is AuthenticateResponse && responseHash.contentEquals(other.responseHash)
    override fun hashCode(): Int = responseHash.contentHashCode()
}

/** Send the per-device remote password (one-shot hash + 20 bytes). */
data class ValidateRemotePassword(val passwordHash: ByteArray) : CLinkRequest {
    init {
        require(passwordHash.size == Authentication.RESPONSE_SIZE) {
            "Password hash must be ${Authentication.RESPONSE_SIZE} bytes"
        }
    }
    override val id = CommandId.ValidatePassword

    override fun serializePayload(): ByteArray = Codec.Writer(1 + Authentication.RESPONSE_SIZE)
        .u8(Authentication.RESPONSE_SIZE)
        .raw(passwordHash)
        .toByteArray()

    override fun equals(other: Any?): Boolean =
        other is ValidateRemotePassword && passwordHash.contentEquals(other.passwordHash)
    override fun hashCode(): Int = passwordHash.contentHashCode()
}

/**
 * Parser for an Authenticate-style response frame.
 *
 * The device sends either:
 *   - `[0x04][len][challenge_bytes...]`  — challenge for our SHA1 round-trip
 *   - shorter ACK form, handled by the response code parser
 *
 * @return [Challenge] when a challenge is found, null otherwise (caller
 *   should treat the frame as a plain ACK).
 */
object AuthChallengeParser {
    /**
     * Parse a DATA frame as the device's challenge.
     *
     * The caller is responsible for routing — invoke only after sending
     * [AuthenticateInit]. Device responds with a DATA frame whose payload
     * begins `[0x04][challengeLen][challengeBytes...]`.
     */
    fun parse(frame: Frame): Challenge? {
        if (!frame.isData) return null
        if (frame.payload.isEmpty()) return null
        // payload[0] is the challenge length (typically 4 in current firmware,
        // but read on-wire rather than gating on a magic marker).
        val len = frame.payload[0].toInt() and 0xFF
        if (len !in 1..32) return null
        if (frame.payload.size < 1 + len) return null
        val challenge = frame.payload.copyOfRange(1, 1 + len)
        return Challenge(challenge, sourceFrame = frame)
    }

    data class Challenge(val bytes: ByteArray, override val sourceFrame: Frame) : CLinkResponse {
        override fun equals(other: Any?): Boolean =
            other is Challenge && bytes.contentEquals(other.bytes)
        override fun hashCode(): Int = bytes.contentHashCode()
    }
}
