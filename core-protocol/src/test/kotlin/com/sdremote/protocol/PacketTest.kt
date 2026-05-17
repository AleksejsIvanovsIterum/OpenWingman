package com.sdremote.protocol

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PacketTest {

    @Test
    fun `build then parse is identity`() {
        val original = Frame.build(command = 0x65, payload = byteArrayOf(0x01, 0x02, 0x03))
        val bytes = original.toByteArray()
        val parsed = Frame.parse(bytes)
        assertTrue(parsed is Frame.ParseResult.Ok)
        parsed as Frame.ParseResult.Ok
        assertEquals(original, parsed.frame)
        assertEquals(bytes.size, parsed.consumedBytes)
    }

    @Test
    fun `parse signals NotEnoughBytes when buffer is short`() {
        val partial = byteArrayOf(0xA5.toByte(), 0x00, 0x05)
        assertEquals(Frame.ParseResult.NotEnoughBytes, Frame.parse(partial))
    }

    @Test
    fun `parse signals BadHeader on wrong start byte`() {
        val mangled = ByteArray(8) { 0xFF.toByte() }
        assertEquals(Frame.ParseResult.BadHeader, Frame.parse(mangled))
    }

    @Test
    fun `parse signals BadChecksum on corruption`() {
        val bytes = Frame.build(command = 0x13, payload = byteArrayOf(0x10, 0x20)).toByteArray()
        // Flip the trailing checksum byte
        bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 0x42).toByte()
        assertEquals(Frame.ParseResult.BadChecksum, Frame.parse(bytes))
    }

    @Test
    fun `frame layout uses 0xA5 header and correct length`() {
        val payload = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
        val bytes = Frame.build(command = 0x12, payload = payload).toByteArray()
        assertEquals(FrameConstants.HEADER, bytes[0])
        // length = payload.size + 1 (command byte)
        assertEquals((payload.size + 1).toByte(), bytes[2])
        assertEquals(0x12.toByte(), bytes[3])
        // Total size = 3 preamble + len + 2 checksum
        assertEquals(3 + (payload.size + 1) + 2, bytes.size)
    }
}
