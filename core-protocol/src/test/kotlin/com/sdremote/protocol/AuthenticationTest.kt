package com.sdremote.protocol

import com.sdremote.protocol.auth.Authentication
import com.sdremote.protocol.commands.AuthChallengeParser
import com.sdremote.protocol.commands.AuthenticateResponse
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AuthenticationTest {

    @Test
    fun `AUTH_KEY is the recovered Sound Devices constant`() {
        // 8 bytes: 38 6A 9B 16 07 68 3A 34 (recovered from Wingman 5.03)
        val expected = byteArrayOf(0x38, 0x6A.toByte(), 0x9B.toByte(), 0x16, 0x07, 0x68, 0x3A, 0x34)
        assertArrayEquals(expected, Authentication.AUTH_KEY)
    }

    @Test
    fun `response is 20-byte SHA1 of challenge appended with auth key`() {
        val challenge = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
        val response = Authentication.computeResponse(challenge)
        assertEquals(Authentication.RESPONSE_SIZE, response.size)

        // Sanity: same input → same hash
        assertArrayEquals(response, Authentication.computeResponse(challenge))

        // Sanity: different challenge → different hash
        val other = Authentication.computeResponse(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x09))
        assert(!response.contentEquals(other))
    }

    @Test
    fun `response packet has 0x01 0x14 prefix and 20 byte hash`() {
        val hash = Authentication.computeResponse(byteArrayOf(1, 2, 3))
        val request = AuthenticateResponse(hash)
        val payload = request.serializePayload()
        assertEquals(22, payload.size)  // 0x01 + 0x14 + 20 hash
        assertEquals(0x01.toByte(), payload[0])
        assertEquals(0x14.toByte(), payload[1])
        assertArrayEquals(hash, payload.copyOfRange(2, 22))
    }

    @Test
    fun `challenge parser extracts bytes after 0x04 prefix`() {
        val challenge = byteArrayOf(0x11, 0x22, 0x33, 0x44)
        val payload = byteArrayOf(0x04, challenge.size.toByte()) + challenge
        val frame = Frame.build(command = CommandId.Authenticate.byte, payload = payload)
        val parsed = AuthChallengeParser.parse(frame)
        assertNotNull(parsed)
        assertArrayEquals(challenge, parsed!!.bytes)
    }

    @Test
    fun `challenge parser returns null on ACK form`() {
        // ACK form — payload starts with 0x00 success, not 0x04 challenge.
        val frame = Frame.build(command = CommandId.Authenticate.byte, payload = byteArrayOf(0x00))
        assertNull(AuthChallengeParser.parse(frame))
    }
}
