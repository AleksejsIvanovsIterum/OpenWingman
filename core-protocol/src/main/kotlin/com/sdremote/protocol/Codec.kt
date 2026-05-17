package com.sdremote.protocol

import java.nio.charset.StandardCharsets

/**
 * Little-endian wire codec primitives for CLink payloads.
 *
 * CLink uses LE for u16/u32/u64. Strings are UTF-8, NUL-terminated.
 */
object Codec {
    // ── Decoding ──
    fun readU8(bytes: ByteArray, offset: Int): Int =
        bytes[offset].toInt() and 0xFF

    fun readU16(bytes: ByteArray, offset: Int): Int {
        val lo = bytes[offset].toInt() and 0xFF
        val hi = bytes[offset + 1].toInt() and 0xFF
        return (hi shl 8) or lo
    }

    fun readU32(bytes: ByteArray, offset: Int): Long {
        val b0 = (bytes[offset].toInt() and 0xFF).toLong()
        val b1 = (bytes[offset + 1].toInt() and 0xFF).toLong()
        val b2 = (bytes[offset + 2].toInt() and 0xFF).toLong()
        val b3 = (bytes[offset + 3].toInt() and 0xFF).toLong()
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    fun readU64(bytes: ByteArray, offset: Int): Long {
        val lo = readU32(bytes, offset)
        val hi = readU32(bytes, offset + 4)
        return lo or (hi shl 32)
    }

    /**
     * Read a NUL-terminated UTF-8 string starting at [offset].
     * If no NUL is found within [bytes], reads to end (defensive against
     * malformed packets).
     */
    fun readString(bytes: ByteArray, offset: Int): String {
        var end = offset
        while (end < bytes.size && bytes[end] != 0.toByte()) end++
        return String(bytes, offset, end - offset, StandardCharsets.UTF_8)
    }

    // ── Encoding ──
    /** Encode `Byte`, `Short`, `Int`, `Long`, `String` (NUL-terminated UTF-8). */
    class Writer(initialCapacity: Int = 16) {
        private val buf = java.io.ByteArrayOutputStream(initialCapacity)

        fun u8(value: Int): Writer = apply { buf.write(value and 0xFF) }
        fun u8(value: Byte): Writer = apply { buf.write(value.toInt() and 0xFF) }

        fun u16(value: Int): Writer = apply {
            buf.write(value and 0xFF)
            buf.write((value ushr 8) and 0xFF)
        }

        fun u32(value: Long): Writer = apply {
            buf.write((value and 0xFF).toInt())
            buf.write(((value ushr 8) and 0xFF).toInt())
            buf.write(((value ushr 16) and 0xFF).toInt())
            buf.write(((value ushr 24) and 0xFF).toInt())
        }

        fun u32(value: Int): Writer = u32(value.toLong() and 0xFFFFFFFFL)

        fun u64(value: Long): Writer = apply {
            u32(value)
            u32(value ushr 32)
        }

        /** UTF-8 NUL-terminated string. */
        fun cstr(s: String): Writer = apply {
            buf.write(s.toByteArray(StandardCharsets.UTF_8))
            buf.write(0)
        }

        /** Raw bytes verbatim. */
        fun raw(bytes: ByteArray): Writer = apply { buf.write(bytes) }

        fun toByteArray(): ByteArray = buf.toByteArray()
    }
}
