package com.sdremote.protocol

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChecksumTest {

    @Test
    fun `compute is deterministic`() {
        val (a1, a2) = Checksum.compute(0x00, 0x12, byteArrayOf(0x01, 0x02, 0x03))
        val (b1, b2) = Checksum.compute(0x00, 0x12, byteArrayOf(0x01, 0x02, 0x03))
        assertEquals(a1, b1)
        assertEquals(a2, b2)
    }

    @Test
    fun `compute differs for different payloads`() {
        val (a1, _) = Checksum.compute(0x00, 0x12, byteArrayOf(0x01, 0x02, 0x03))
        val (b1, _) = Checksum.compute(0x00, 0x12, byteArrayOf(0x01, 0x02, 0x04))
        assertFalse(a1 == b1, "Different payload should yield different ck1")
    }

    @Test
    fun `verify accepts freshly built packet`() {
        val frame = Frame.build(command = 0x13, payload = byteArrayOf(0x10, 0x20))
        assertTrue(Checksum.verify(frame.toByteArray()))
    }

    @Test
    fun `verify rejects flipped byte in payload`() {
        val bytes = Frame.build(command = 0x13, payload = byteArrayOf(0x10, 0x20)).toByteArray()
        bytes[FrameConstants.IX_PAYLOAD_START] = (bytes[FrameConstants.IX_PAYLOAD_START].toInt() xor 0x01).toByte()
        assertFalse(Checksum.verify(bytes))
    }

    @Test
    fun `unit id is substituted with zero for non-broadcast`() {
        // Two packets, same content, different non-broadcast unitId — same checksum.
        val a = Frame.build(command = 0x13, payload = byteArrayOf(0x10), unitId = 0x05)
        val b = Frame.build(command = 0x13, payload = byteArrayOf(0x10), unitId = 0x77)
        assertEquals(a.checksum1, b.checksum1)
        assertEquals(a.checksum2, b.checksum2)
    }

    @Test
    fun `broadcast unit id participates in checksum`() {
        val a = Frame.build(command = 0x13, payload = byteArrayOf(0x10),
            unitId = FrameConstants.UNIT_BROADCAST)
        val b = Frame.build(command = 0x13, payload = byteArrayOf(0x10), unitId = 0x00)
        assertFalse(a.checksum1 == b.checksum1 && a.checksum2 == b.checksum2,
            "Broadcast unit should produce a different checksum than 0x00")
    }
}
