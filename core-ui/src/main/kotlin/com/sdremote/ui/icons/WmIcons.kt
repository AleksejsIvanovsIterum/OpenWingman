package com.sdremote.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// 1:1 translation of the WMIcon set from wingman-primitives.jsx.
// Same 24×24 viewbox, stroke 1.5, round caps/joins. Adjust strokeWidth at the
// call site if needed (e.g. 2.2 for the "false" cross).

private const val VB = 24f

/**
 * Build a stroke-only icon (most of the set).
 * Filled icons (record, stop, play, circle dot) use [buildFilled] instead.
 */
private fun buildStroke(name: String, strokeWidth: Float = 1.5f, block: WmPathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = VB, viewportHeight = VB,
    ).also { builder ->
        WmPathBuilder(builder, strokeWidth = strokeWidth, fillColor = null).block()
    }.build()

private fun buildFilled(name: String, strokeWidth: Float = 1.5f, block: WmPathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = VB, viewportHeight = VB,
    ).also { builder ->
        WmPathBuilder(builder, strokeWidth = strokeWidth, fillColor = SolidColor(Color.Black)).block()
    }.build()

/** Thin wrapper so the call-site DSL stays compact. */
private class WmPathBuilder(
    val builder: ImageVector.Builder,
    val strokeWidth: Float,
    val fillColor: SolidColor?,
) {
    /** Stroke-only path. */
    fun stroke(pathData: String) {
        builder.path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero,
            pathBuilder = { parseInto(pathData) },
        )
    }

    /** Fill-only path (no outline). */
    fun fill(pathData: String) {
        builder.path(
            fill = SolidColor(Color.Black),
            stroke = null,
            pathFillType = PathFillType.NonZero,
            pathBuilder = { parseInto(pathData) },
        )
    }
}

/** Hand-written SVG-path mini-parser for the small command set we use. */
private fun androidx.compose.ui.graphics.vector.PathBuilder.parseInto(d: String) {
    var i = 0
    var cmd = ' '
    fun skipWs() { while (i < d.length && (d[i] == ' ' || d[i] == ',')) i++ }
    fun readNum(): Float {
        skipWs()
        val start = i
        if (i < d.length && (d[i] == '-' || d[i] == '+')) i++
        while (i < d.length && (d[i].isDigit() || d[i] == '.')) i++
        return d.substring(start, i).toFloat()
    }
    while (i < d.length) {
        skipWs()
        if (i >= d.length) break
        val c = d[i]
        if (c.isLetter()) { cmd = c; i++; continue }
        val isAbs = cmd.isUpperCase()
        when (cmd.lowercaseChar()) {
            'm' -> { val x = readNum(); val y = readNum(); if (isAbs) moveTo(x, y) else moveToRelative(x, y) }
            'l' -> { val x = readNum(); val y = readNum(); if (isAbs) lineTo(x, y) else lineToRelative(x, y) }
            'h' -> { val x = readNum();                   if (isAbs) horizontalLineTo(x) else horizontalLineToRelative(x) }
            'v' -> { val y = readNum();                   if (isAbs) verticalLineTo(y) else verticalLineToRelative(y) }
            'c' -> {
                val x1 = readNum(); val y1 = readNum()
                val x2 = readNum(); val y2 = readNum()
                val x  = readNum(); val y  = readNum()
                if (isAbs) curveTo(x1, y1, x2, y2, x, y) else curveToRelative(x1, y1, x2, y2, x, y)
            }
            'a' -> {
                val rx = readNum(); val ry = readNum(); val rot = readNum()
                // SVG path flags may be concatenated with the next coordinate
                // (e.g. "0122" means flag=0, flag=1, x=22). Read each flag as
                // a single character.
                fun readFlag(): Boolean {
                    skipWs()
                    val ch = d[i]; i++
                    return ch == '1'
                }
                val laf = readFlag()
                val sf = readFlag()
                val x = readNum(); val y = readNum()
                if (isAbs) arcTo(rx, ry, rot, laf, sf, x, y) else arcToRelative(rx, ry, rot, laf, sf, x, y)
            }
            'z' -> close()
            else -> { /* unsupported — bail */ break }
        }
    }
}

object WmIcons {
    // Filled marks — circles use arcTo (a) commands; PathBuilder has no addCircle.
    val Record  = buildStroke("record")  { fill("M6 12a6 6 0 1 0 12 0a6 6 0 1 0 -12 0") }
    val Stop    = buildStroke("stop")    { fill("M6 6h12v12h-12z") }
    val Play    = buildStroke("play")    { fill("M7 5L19 12L7 19z") }
    val Circle  = buildStroke("circle")  {
        stroke("M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0")
        fill("M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0")
    }
    val FalseX  = buildStroke("false", strokeWidth = 2.2f) {
        stroke("M5 5l14 14M19 5L5 19")
    }
    val Plus    = buildStroke("plus")     { stroke("M12 5v14M5 12h14") }
    val Minus   = buildStroke("minus")    { stroke("M5 12h14") }
    val Edit    = buildStroke("edit")     { stroke("M4 20h4l10-10-4-4L4 16v4z"); stroke("M14 6l4 4") }
    val Mic     = buildStroke("mic")      { stroke("M9 3 h6 v12 h-6 z"); stroke("M5 11a7 7 0 0014 0M12 18v3") }
    val Clap    = buildStroke("clap")     { stroke("M3 9l18-3v12H3V9z"); stroke("M3 9l18-3M7 7.7v3M11 7v3M15 6.3v3M19 5.7v3") }
    val List    = buildStroke("list")     { stroke("M8 6h12M8 12h12M8 18h12M4 6h.01M4 12h.01M4 18h.01") }
    val Report  = buildStroke("report")   { stroke("M6 3h9l4 4v14H6V3z"); stroke("M14 3v5h5M9 13h6M9 17h6M9 9h2") }
    val Settings = buildStroke("settings") {
        stroke("M19.4 15a1.7 1.7 0 00.3 1.8l.1.1a2 2 0 11-2.8 2.8l-.1-.1a1.7 1.7 0 00-1.8-.3 1.7 1.7 0 00-1 1.5V21a2 2 0 11-4 0v-.1a1.7 1.7 0 00-1-1.5 1.7 1.7 0 00-1.8.3l-.1.1a2 2 0 11-2.8-2.8l.1-.1a1.7 1.7 0 00.3-1.8 1.7 1.7 0 00-1.5-1H3a2 2 0 110-4h.1a1.7 1.7 0 001.5-1 1.7 1.7 0 00-.3-1.8l-.1-.1a2 2 0 112.8-2.8l.1.1a1.7 1.7 0 001.8.3H9a1.7 1.7 0 001-1.5V3a2 2 0 114 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.8-.3l.1-.1a2 2 0 112.8 2.8l-.1.1a1.7 1.7 0 00-.3 1.8V9a1.7 1.7 0 001.5 1H21a2 2 0 110 4h-.1a1.7 1.7 0 00-1.5 1z")
        stroke("M9 12a3 3 0 106 0 3 3 0 00-6 0z")
    }
    val ChevronRight = buildStroke("chevron-right") { stroke("M9 6l6 6-6 6") }
    val ChevronDown  = buildStroke("chevron-down")  { stroke("M6 9l6 6 6-6") }
    val Search  = buildStroke("search")   { stroke("M4 11a7 7 0 1014 0 7 7 0 00-14 0zM21 21l-4.3-4.3") }
    val Wifi    = buildStroke("wifi")     { stroke("M2 8.5A15 15 0 0122 8.5M5 12.5a10 10 0 0114 0M8.5 16.5a5 5 0 017 0"); fill("M11 20a1 1 0 102 0 1 1 0 00-2 0z") }
    val Bluetooth = buildStroke("bt")     { stroke("M7 7l10 10-5 4V3l5 4L7 17") }
    val Check   = buildStroke("check")    { stroke("M5 12l5 5L20 7") }
    val Flag    = buildStroke("flag")     { stroke("M5 21V4M5 4h11l-2 4 2 4H5") }
    val Lock    = buildStroke("lock")     { stroke("M5 11h14v10H5z"); stroke("M8 11V8a4 4 0 018 0v3") }
    val Sun     = buildStroke("sun")      { stroke("M8 12a4 4 0 108 0 4 4 0 00-8 0zM12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4") }
    val Moon    = buildStroke("moon")     { stroke("M20 14.5A8.5 8.5 0 119.5 4 7 7 0 0020 14.5z") }
    val At      = buildStroke("at")       { stroke("M8 12a4 4 0 108 0 4 4 0 00-8 0zM16 12v1.5a2.5 2.5 0 005 0V12a9 9 0 10-3.5 7.1") }
}
