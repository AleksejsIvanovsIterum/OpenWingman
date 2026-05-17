package com.sdremote.domain

/**
 * Logging sink for [Session]. Pure-Kotlin contract so domain stays free of
 * Android deps; wire to Timber / logcat from the app layer.
 */
fun interface SessionLogger {
    fun log(line: String)

    companion object {
        val NoOp = SessionLogger { _ -> }
    }
}

/** Hex dump helper for byte buffers — useful in log messages. */
fun ByteArray.toHexLog(maxBytes: Int = 64): String {
    val n = size.coerceAtMost(maxBytes)
    val sb = StringBuilder(n * 3)
    for (i in 0 until n) {
        if (i > 0) sb.append(' ')
        sb.append("%02x".format(this[i]))
    }
    if (size > maxBytes) sb.append(" …+").append(size - maxBytes).append('B')
    return sb.toString()
}
