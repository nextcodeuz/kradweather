package com.krad.weather.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Internetga bog'liq bo'lmagan, maxsus chizilgan premium ob-havo ikonkalar.
 * OpenWeather icon kodlari: 01 quyosh/oy, 02-04 bulut, 09-10 yomg'ir,
 * 11 chaqmoq, 13 qor, 50 tuman. Kunduz (d) / tun (n) ajratiladi.
 *
 * SMOOTHLIK: animatsiya [graphicsLayer] + [Animatable] orqali RenderThread'da
 * ishlaydi — har freymda recomposition BO'LMAYDI, 60fps silliq.
 */

private val SunCore = Color(0xFFFFD54F)
private val SunEdge = Color(0xFFFF9E3D)
private val MoonPale = Color(0xFFFFF6D9)
private val CloudLight = Color(0xFFFFFFFF)
private val CloudShade = Color(0xFFCFD8DC)
private val CloudDark = Color(0xFF90A4AE)
private val RainBlue = Color(0xFF4FC3F7)
private val BoltYellow = Color(0xFFFFEB3B)
private val BoltEdge = Color(0xFFF9A825)

private fun DrawScope.sunDisc(cx: Float, cy: Float, r: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(SunCore, SunEdge),
            center = Offset(cx - r * 0.25f, cy - r * 0.25f),
            radius = r * 1.4f
        ),
        radius = r,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.sunRays(cx: Float, cy: Float, r: Float) {
    for (i in 0 until 8) {
        val a = Math.toRadians((i * 45).toDouble())
        val x1 = cx + cos(a).toFloat() * r * 1.35f
        val y1 = cy + sin(a).toFloat() * r * 1.35f
        val x2 = cx + cos(a).toFloat() * r * 1.8f
        val y2 = cy + sin(a).toFloat() * r * 1.8f
        drawLine(
            SunCore, Offset(x1, y1), Offset(x2, y2),
            strokeWidth = r * 0.22f, cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.sun(cx: Float, cy: Float, r: Float) {
    sunRays(cx, cy, r)
    sunDisc(cx, cy, r)
}

private fun DrawScope.moon(cx: Float, cy: Float, r: Float) {
    val crescent = Path().apply {
        addOval(Rect(Offset(cx - r, cy - r), Size(r * 2, r * 2)))
        addOval(
            Rect(
                Offset(cx - r * 0.35f, cy - r * 1.2f),
                Size(r * 2, r * 2)
            )
        )
        fillType = PathFillType.EvenOdd
    }
    drawPath(crescent, MoonPale)
    val stars = listOf(
        Triple(cx + r * 1.5f, cy - r * 0.9f, r * 0.16f),
        Triple(cx + r * 1.1f, cy + r * 1.2f, r * 0.12f),
        Triple(cx + r * 1.9f, cy + r * 0.4f, r * 0.1f)
    )
    stars.forEach { (x, y, s) ->
        drawLine(Color.White, Offset(x - s, y), Offset(x + s, y), s * 0.5f, StrokeCap.Round)
        drawLine(Color.White, Offset(x, y - s), Offset(x, y + s), s * 0.5f, StrokeCap.Round)
    }
}

private fun DrawScope.cloud(
    cx: Float,
    cy: Float,
    w: Float,
    dark: Boolean = false
) {
    val body = if (dark) CloudDark else CloudLight
    val shade = CloudShade.copy(alpha = 0.85f)
    val r = w * 0.22f
    drawCircle(shade, r, Offset(cx - w * 0.22f, cy + r * 0.25f))
    drawCircle(shade, r * 1.25f, Offset(cx + w * 0.02f, cy - r * 0.1f))
    drawCircle(shade, r * 0.9f, Offset(cx + w * 0.24f, cy + r * 0.25f))
    drawCircle(body, r, Offset(cx - w * 0.22f, cy))
    drawCircle(body, r * 1.25f, Offset(cx + w * 0.02f, cy - r * 0.35f))
    drawCircle(body, r * 0.9f, Offset(cx + w * 0.24f, cy))
    roundedBase(body, cx - w * 0.42f, cy - r * 0.1f, cx + w * 0.42f, cy + r * 0.95f, r * 0.9f)
}

private fun DrawScope.roundedBase(
    color: Color,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float
) {
    drawRect(color, Offset(left + radius, top), Size(right - left - 2 * radius, bottom - top))
    drawRect(color, Offset(left, top + radius), Size(right - left, bottom - top - radius))
    drawCircle(color, radius, Offset(left + radius, top + radius))
    drawCircle(color, radius, Offset(right - radius, top + radius))
    drawCircle(color, radius, Offset(left + radius, bottom - radius))
    drawCircle(color, radius, Offset(right - radius, bottom - radius))
}

/** Bitta yomg'ir tomchisi (statik holat uchun). */
private fun DrawScope.rainDrop(x: Float, y: Float, len: Float, width: Float, alpha: Float) {
    drawLine(
        RainBlue.copy(alpha = alpha),
        Offset(x, y),
        Offset(x - len * 0.4f, y + len),
        strokeWidth = width,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.rainStatic(cx: Float, cy: Float, w: Float) {
    // Tomchilar pastga qarab so'nib boradi (harakatsiz, lekin chiroyli)
    for (i in 0 until 3) {
        val x = cx - w * 0.2f + i * w * 0.2f
        val drop = 0.25f + i * 0.25f
        rainDrop(x, cy + drop * w * 0.22f, w * 0.1f, w * 0.035f, 1f - drop * 0.6f)
    }
}

/** Bitta qor parchasi (6 qirrali). */
private fun DrawScope.snowFlake(x: Float, y: Float, r: Float, width: Float) {
    for (k in 0 until 3) {
        val a = Math.toRadians((k * 60).toDouble())
        drawLine(
            Color.White,
            Offset(x - cos(a).toFloat() * r, y - sin(a).toFloat() * r),
            Offset(x + cos(a).toFloat() * r, y + sin(a).toFloat() * r),
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.snowStatic(cx: Float, cy: Float, w: Float) {
    for (i in 0 until 3) {
        val x = cx - w * 0.18f + i * w * 0.18f
        snowFlake(x, cy + (0.2f + i * 0.22f) * w * 0.24f, w * 0.035f, w * 0.022f)
    }
}

private fun DrawScope.bolt(cx: Float, cy: Float, s: Float) {
    val bolt = Path().apply {
        moveTo(cx + s * 0.08f, cy)
        lineTo(cx - s * 0.12f, cy + s * 0.42f)
        lineTo(cx + s * 0.02f, cy + s * 0.42f)
        lineTo(cx - s * 0.08f, cy + s * 0.8f)
        lineTo(cx + s * 0.2f, cy + s * 0.36f)
        lineTo(cx + s * 0.05f, cy + s * 0.36f)
        close()
    }
    drawPath(bolt, BoltEdge)
    val inner = Path().apply {
        moveTo(cx + s * 0.08f, cy + s * 0.05f)
        lineTo(cx - s * 0.06f, cy + s * 0.4f)
        lineTo(cx + s * 0.03f, cy + s * 0.4f)
        lineTo(cx - s * 0.02f, cy + s * 0.7f)
        lineTo(cx + s * 0.14f, cy + s * 0.36f)
        lineTo(cx + s * 0.03f, cy + s * 0.36f)
        close()
    }
    drawPath(inner, BoltYellow)
}

private fun DrawScope.mist(cx: Float, cy: Float, w: Float) {
    val ys = listOf(-0.06f, 0.08f, 0.22f)
    ys.forEachIndexed { i, dy ->
        val ww = w * (0.7f - i * 0.1f)
        drawLine(
            Color.White.copy(alpha = 0.9f),
            Offset(cx - ww / 2, cy + w * dy),
            Offset(cx + ww / 2, cy + w * dy),
            strokeWidth = w * 0.05f,
            cap = StrokeCap.Round
        )
    }
}

/** Harakatsiz chizma (ro'yxatlar uchun — maksimal tez). */
private fun DrawScope.drawStaticArt(code: String, isNight: Boolean, group: String) {
    val s = size.minDimension
    val cx = size.width / 2f
    when (group) {
        "01" -> if (isNight) moon(cx, size.height * 0.44f, s * 0.26f)
        else sun(cx, size.height / 2f, s * 0.24f)
        "02" -> {
            if (isNight) moon(cx - s * 0.12f, size.height * 0.36f, s * 0.2f)
            else sun(cx - s * 0.14f, size.height * 0.36f, s * 0.19f)
            cloud(cx + s * 0.06f, size.height * 0.58f, s * 0.78f)
        }
        "03" -> cloud(cx, size.height * 0.5f, s * 0.85f)
        "04" -> {
            cloud(cx - s * 0.08f, size.height * 0.4f, s * 0.7f, dark = true)
            cloud(cx + s * 0.1f, size.height * 0.6f, s * 0.8f)
        }
        "09", "10" -> {
            cloud(cx, size.height * 0.42f, s * 0.85f, dark = group == "09")
            rainStatic(cx, size.height * 0.62f, s * 0.85f)
        }
        "11" -> {
            cloud(cx, size.height * 0.38f, s * 0.85f, dark = true)
            bolt(cx, size.height * 0.55f, s * 0.5f)
        }
        "13" -> {
            cloud(cx, size.height * 0.4f, s * 0.85f)
            snowStatic(cx, size.height * 0.6f, s * 0.85f)
        }
        "50" -> {
            if (isNight) moon(cx, size.height * 0.3f, s * 0.18f)
            else sun(cx, size.height * 0.3f, s * 0.18f)
            mist(cx, size.height * 0.62f, s * 0.9f)
        }
        else -> sun(cx, size.height / 2f, s * 0.24f)
    }
}

@Composable
fun WeatherArt(
    iconCode: String,
    modifier: Modifier = Modifier,
    animated: Boolean = false
) {
    val code = iconCode.ifBlank { "01d" }
    val isNight = code.endsWith("n")
    val group = code.take(2)

    if (!animated) {
        Canvas(modifier = modifier) { drawStaticArt(code, isNight, group) }
        return
    }

    // ---- Animatsiyali hero: faqat RenderThread harakatlanadi ----
    val rotation = remember(code) { Animatable(0f) }
    val fall = remember(code) { Animatable(0f) }
    val sway = remember(code) { Animatable(0f) }
    val blink = remember(code) { Animatable(1f) }
    LaunchedEffect(code) {
        launch { rotation.animateTo(360f, infiniteRepeatable(tween(14000, easing = LinearEasing))) }
        launch { fall.animateTo(1f, infiniteRepeatable(tween(1300, easing = LinearEasing))) }
        launch { sway.animateTo(1f, infiniteRepeatable(tween(2600, easing = LinearEasing))) }
        launch {
            blink.animateTo(
                0.55f,
                infiniteRepeatable(
                    tween(900, easing = LinearEasing),
                    androidx.compose.animation.core.RepeatMode.Reverse
                )
            )
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val wPx = remember(density, maxWidth) { with(density) { maxWidth.toPx() } }
        val hPx = remember(density, maxHeight) { with(density) { maxHeight.toPx() } }
        val s = minOf(wPx, hPx)
        val cx = wPx / 2f

        // 1) Statik asos (bir marta chiziladi)
        Canvas(Modifier.fillMaxSize()) {
            when (group) {
                "01" -> if (isNight) moon(cx, hPx * 0.44f, s * 0.26f)
                else sunDisc(cx, hPx / 2f, s * 0.24f)
                "02" -> {
                    if (isNight) moon(cx - s * 0.12f, hPx * 0.36f, s * 0.2f)
                    else sunDisc(cx - s * 0.14f, hPx * 0.36f, s * 0.19f)
                    cloud(cx + s * 0.06f, hPx * 0.58f, s * 0.78f)
                }
                "03" -> cloud(cx, hPx * 0.5f, s * 0.85f)
                "04" -> {
                    cloud(cx - s * 0.08f, hPx * 0.4f, s * 0.7f, dark = true)
                    cloud(cx + s * 0.1f, hPx * 0.6f, s * 0.8f)
                }
                "09", "10" -> cloud(cx, hPx * 0.42f, s * 0.85f, dark = group == "09")
                "11" -> cloud(cx, hPx * 0.38f, s * 0.85f, dark = true)
                "13" -> cloud(cx, hPx * 0.4f, s * 0.85f)
                "50" -> {
                    if (isNight) moon(cx, hPx * 0.3f, s * 0.18f)
                    else sunDisc(cx, hPx * 0.3f, s * 0.18f)
                    mist(cx, hPx * 0.62f, s * 0.9f)
                }
                else -> sunDisc(cx, hPx / 2f, s * 0.24f)
            }
        }

        // 2) Aylanuvchi quyosh nurlari
        if (!isNight && (group == "01" || group == "02" || group == "50")) {
            val rcx = if (group == "01") cx else cx - s * 0.14f
            val rcy = if (group == "01") hPx / 2f else hPx * (if (group == "02") 0.36f else 0.3f)
            val rr = s * (if (group == "01") 0.24f else if (group == "02") 0.19f else 0.18f)
            Canvas(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotation.value
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(
                            rcx / wPx, rcy / hPx
                        )
                    }
            ) {
                sunRays(rcx, rcy, rr)
            }
        }

        // 3) Yomg'ir tomchilari (pastga tushadi + so'nadi)
        if (group == "09" || group == "10") {
            val baseY = hPx * 0.62f
            val w = s * 0.85f
            for (i in 0 until 3) {
                val x = cx - w * 0.2f + i * w * 0.2f
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val p = (fall.value + i * 0.33f) % 1f
                            translationY = p * w * 0.22f
                            alpha = 1f - p * 0.6f
                        }
                ) {
                    rainDrop(x, baseY, w * 0.1f, w * 0.035f, 1f)
                }
            }
        }

        // 4) Qor parchalari (tushadi + yon tomonga tebranadi)
        if (group == "13") {
            val baseY = hPx * 0.6f
            val w = s * 0.85f
            for (i in 0 until 3) {
                val x0 = cx - w * 0.18f + i * w * 0.18f
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val p = (sway.value + i * 0.33f) % 1f
                            translationY = p * w * 0.24f
                            translationX = sin(p * 6.28f).toFloat() * w * 0.02f
                            alpha = 1f - p * 0.4f
                        }
                ) {
                    snowFlake(x0, baseY, w * 0.035f, w * 0.022f)
                }
            }
        }

        // 5) Chaqmoq miltillashi
        if (group == "11") {
            Canvas(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = blink.value }
            ) {
                bolt(cx, hPx * 0.55f, s * 0.5f)
            }
        }
    }
}
