package com.sdremote.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Curated set of 24×24 icons used across the app.
//
// Hand-built with explicit PathBuilder calls — no SVG string parser, so
// the static initializer of [WmIcons] can never throw on bad path data.
//
// All icons paint in black; callers tint via Compose's Icon(tint = ...)
// composable so re-use across themes is trivial.

private const val VB = 24f

private fun strokeOnly(name: String, strokeWidth: Float = 1.5f, block: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, VB, VB)
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            block = block,
        )
        .build()

private fun filled(name: String, block: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, VB, VB)
        .addPath(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero,
            block = block,
        )
        .build()

private fun ImageVector.Builder.addPath(
    fill: SolidColor? = null,
    stroke: SolidColor? = null,
    strokeLineWidth: Float = 0f,
    strokeLineCap: StrokeCap = StrokeCap.Butt,
    strokeLineJoin: StrokeJoin = StrokeJoin.Miter,
    pathFillType: PathFillType = PathFillType.NonZero,
    block: PathBuilder.() -> Unit,
): ImageVector.Builder = apply {
    path(
        fill = fill,
        stroke = stroke,
        strokeLineWidth = strokeLineWidth,
        strokeLineCap = strokeLineCap,
        strokeLineJoin = strokeLineJoin,
        pathFillType = pathFillType,
        pathBuilder = block,
    )
}

/** Add a circle to a path using two semicircular arcs. */
private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = cx + r, y1 = cy)
    arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = cx - r, y1 = cy)
    close()
}

object WmIcons {
    // ── Transport / filled marks ──
    val Record = filled("record") { circle(12f, 12f, 6f) }

    val Stop = filled("stop") {
        moveTo(6f, 6f); lineTo(18f, 6f); lineTo(18f, 18f); lineTo(6f, 18f); close()
    }

    val Play = filled("play") {
        moveTo(7f, 5f); lineTo(19f, 12f); lineTo(7f, 19f); close()
    }

    val Circle: ImageVector = ImageVector.Builder("circle", 24.dp, 24.dp, VB, VB)
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { circle(12f, 12f, 8f) }
        .addPath(fill = SolidColor(Color.Black)) { circle(12f, 12f, 3f) }
        .build()

    // ── Cross / actions ──
    val FalseX = strokeOnly("false_x", strokeWidth = 2.2f) {
        moveTo(5f, 5f); lineTo(19f, 19f)
        moveTo(19f, 5f); lineTo(5f, 19f)
    }

    val Plus = strokeOnly("plus") {
        moveTo(12f, 5f); lineTo(12f, 19f)
        moveTo(5f, 12f); lineTo(19f, 12f)
    }

    val Minus = strokeOnly("minus") {
        moveTo(5f, 12f); lineTo(19f, 12f)
    }

    val Edit = strokeOnly("edit") {
        moveTo(4f, 20f); lineTo(8f, 20f); lineTo(18f, 10f); lineTo(14f, 6f); lineTo(4f, 16f); close()
        moveTo(14f, 6f); lineTo(18f, 10f)
    }

    val Check = strokeOnly("check") {
        moveTo(5f, 12f); lineTo(10f, 17f); lineTo(20f, 7f)
    }

    // ── Lists & content ──
    val List = strokeOnly("list") {
        moveTo(8f, 6f); lineTo(20f, 6f)
        moveTo(8f, 12f); lineTo(20f, 12f)
        moveTo(8f, 18f); lineTo(20f, 18f)
        // Three bullet dots on the left.
        moveTo(4.5f, 6f); lineTo(4.5f, 6f)
        moveTo(4.5f, 12f); lineTo(4.5f, 12f)
        moveTo(4.5f, 18f); lineTo(4.5f, 18f)
    }

    val Report = strokeOnly("report") {
        // Document outline with folded corner.
        moveTo(6f, 3f); lineTo(15f, 3f); lineTo(19f, 7f); lineTo(19f, 21f); lineTo(6f, 21f); close()
        moveTo(14f, 3f); lineTo(14f, 8f); lineTo(19f, 8f)
        // Body lines.
        moveTo(9f, 13f); lineTo(15f, 13f)
        moveTo(9f, 17f); lineTo(15f, 17f)
        moveTo(9f, 9f); lineTo(11f, 9f)
    }

    val Settings: ImageVector = ImageVector.Builder("settings", 24.dp, 24.dp, VB, VB)
        // Center dot.
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { circle(12f, 12f, 3f) }
        // Outer gear ring approximated as 8-pointed star.
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            val rOuter = 9.5f
            val rInner = 7f
            val cx = 12f; val cy = 12f
            val points = 8
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) rOuter else rInner
                val a = Math.PI * 2 * i / (points * 2)
                val x = cx + r * Math.cos(a).toFloat()
                val y = cy + r * Math.sin(a).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        .build()

    // ── Chevrons ──
    val ChevronRight = strokeOnly("chevron_right") {
        moveTo(9f, 6f); lineTo(15f, 12f); lineTo(9f, 18f)
    }

    val ChevronDown = strokeOnly("chevron_down") {
        moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f)
    }

    // ── Search ──
    val Search: ImageVector = ImageVector.Builder("search", 24.dp, 24.dp, VB, VB)
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { circle(11f, 11f, 7f) }
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { moveTo(16.5f, 16.5f); lineTo(21f, 21f) }
        .build()

    // ── Connectivity ──
    val Wifi: ImageVector = ImageVector.Builder("wifi", 24.dp, 24.dp, VB, VB)
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // Three concentric arcs facing up.
            moveTo(2f, 8.5f); arcTo(15f, 15f, 0f, false, true, 22f, 8.5f)
            moveTo(5f, 12.5f); arcTo(10f, 10f, 0f, false, true, 19f, 12.5f)
            moveTo(8.5f, 16.5f); arcTo(5f, 5f, 0f, false, true, 15.5f, 16.5f)
        }
        .addPath(fill = SolidColor(Color.Black)) { circle(12f, 20f, 1f) }
        .build()

    val Bluetooth = strokeOnly("bluetooth") {
        moveTo(7f, 7f); lineTo(17f, 17f); lineTo(12f, 21f); lineTo(12f, 3f); lineTo(17f, 7f); lineTo(7f, 17f)
    }

    // ── Indicators ──
    val Flag = strokeOnly("flag") {
        moveTo(5f, 21f); lineTo(5f, 4f)
        moveTo(5f, 4f); lineTo(16f, 4f); lineTo(14f, 8f); lineTo(16f, 12f); lineTo(5f, 12f)
    }

    val Lock = strokeOnly("lock") {
        moveTo(5f, 11f); lineTo(19f, 11f); lineTo(19f, 21f); lineTo(5f, 21f); close()
        moveTo(8f, 11f); lineTo(8f, 8f)
        arcTo(4f, 4f, 0f, false, true, 16f, 8f)
        lineTo(16f, 11f)
    }

    // ── Theme ──
    val Sun: ImageVector = ImageVector.Builder("sun", 24.dp, 24.dp, VB, VB)
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { circle(12f, 12f, 4f) }
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // 8 rays.
            val cx = 12f; val cy = 12f; val rIn = 6f; val rOut = 9f
            for (i in 0 until 8) {
                val a = Math.PI * 2 * i / 8
                val x1 = cx + rIn * Math.cos(a).toFloat()
                val y1 = cy + rIn * Math.sin(a).toFloat()
                val x2 = cx + rOut * Math.cos(a).toFloat()
                val y2 = cy + rOut * Math.sin(a).toFloat()
                moveTo(x1, y1); lineTo(x2, y2)
            }
        }
        .build()

    val Moon = strokeOnly("moon") {
        // Crescent — quick approximation with two arcs.
        moveTo(20f, 14.5f)
        arcTo(8.5f, 8.5f, 0f, true, true, 9.5f, 4f)
        arcTo(7f, 7f, 0f, false, false, 20f, 14.5f)
        close()
    }

    // ── Inputs / metadata ──
    val Mic = strokeOnly("mic") {
        moveTo(9f, 3f); lineTo(15f, 3f); lineTo(15f, 15f); lineTo(9f, 15f); close()
        moveTo(5f, 11f); arcTo(7f, 7f, 0f, false, false, 19f, 11f)
        moveTo(12f, 18f); lineTo(12f, 21f)
    }

    val Clap = strokeOnly("clap") {
        // Clapperboard body.
        moveTo(3f, 9f); lineTo(21f, 6f); lineTo(21f, 18f); lineTo(3f, 18f); close()
        // Top hinge ticks.
        moveTo(7f, 7.7f); lineTo(7f, 10.7f)
        moveTo(11f, 7f); lineTo(11f, 10f)
        moveTo(15f, 6.3f); lineTo(15f, 9.3f)
        moveTo(19f, 5.7f); lineTo(19f, 8.7f)
    }

    val At: ImageVector = ImageVector.Builder("at", 24.dp, 24.dp, VB, VB)
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { circle(12f, 12f, 4f) }
        .addPath(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            // Outer "@" loop with tail.
            moveTo(16f, 12f)
            lineTo(16f, 13.5f)
            arcTo(2.5f, 2.5f, 0f, false, false, 21f, 13.5f)
            lineTo(21f, 12f)
            arcTo(9f, 9f, 0f, true, false, 17.5f, 19.1f)
        }
        .build()
}
