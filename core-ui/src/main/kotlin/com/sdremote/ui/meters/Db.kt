package com.sdremote.ui.meters

/**
 * Map a dBFS reading to a 0..1 position on a meter scale.
 *
 * Linear clamping in dB-space (not amplitude) — matches the JSX
 * `wmDbToPct` function. This is what every modern broadcast meter does:
 * the eye reads dB linearly above ~−20.
 *
 * @param db    measured value, dBFS
 * @param min   bottom of the scale (default −60)
 * @param max   top of the scale (default +20, so 0 dBFS sits 75% up)
 */
fun dbToPct(db: Float, min: Float = -60f, max: Float = 20f): Float {
    val c = db.coerceIn(min, max)
    return (c - min) / (max - min)
}

/** Whether a peak reading should trip the clip indicator. */
fun isClipping(peak: Float): Boolean = peak > -0.5f

/** Whether a peak reading is "alive" (not just resting at the floor). */
fun isVisible(peak: Float, min: Float = -60f): Boolean = peak > (min + 5f)

/** Channel display state. Off = no signal expected, Mute = soft-muted, etc. */
enum class ChannelState { Record, Mute, Solo, Off }
