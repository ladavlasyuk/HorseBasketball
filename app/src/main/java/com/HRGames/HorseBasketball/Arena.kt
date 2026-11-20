package com.HRGames.HorseBasketball

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import kotlin.math.min
import kotlin.math.sin

internal class ArenaArt(res: Resources) {
    private val horse = BitmapFactory.decodeResource(res, R.drawable.horse)
    private val ball = BitmapFactory.decodeResource(res, R.drawable.ball)
    private val hoop = BitmapFactory.decodeResource(res, R.drawable.hoop)
    private val bg = BitmapFactory.decodeResource(res, R.drawable.bg)
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val box = RectF()
    private val rivalTint = PorterDuffColorFilter(0xff8e96bd.toInt(), PorterDuff.Mode.MULTIPLY)

    private fun sprite(c: Canvas, bitmap: android.graphics.Bitmap, x: Float, y: Float, w: Float, h: Float) {
        box.set(x, y, x + w, y + h)
        c.drawBitmap(bitmap, null, box, ink)
    }

    private fun floor(c: Canvas, w: Float, h: Float, offset: Float) {
        ink.color = -1
        sprite(c, bg, 0f, 0f, w, h)
        ink.color = 0x29c4dcff
        ink.strokeWidth = 1.3f
        for (i in 0..2) {
            val y = h * (.79f + .095f * i)
            c.drawLine(0f, y, w, y, ink)
        }
        var x = -offset % 115f
        while (x < w + 30f) {
            c.drawLine(x, h * .91f, x + 26, h * .91f, ink)
            x += 115
        }
        ink.color = 0x448cb9ff
        for (i in 0 until 24) {
            for (j in 0..3) c.drawCircle(w - i * 11f, 14f + j * 10, 1.35f, ink)
        }
        ink.color = -1
    }

    private fun rider(c: Canvas, x: Float, ground: Float, width: Float, phase: Float, rival: Boolean) {
        val rise = sin(phase) * 2.6f
        val h = width * horse.height / horse.width
        ink.color = 0x62010c32
        c.drawOval(x + width * .17f, ground - 4, x + width * .90f, ground + 5, ink)
        ink.color = -1
        if (rival) ink.colorFilter = rivalTint
        c.save()
        c.rotate(sin(phase) * 1.3f, x + width * .5f, ground - h * .44f)
        sprite(c, horse, x, ground - h + rise, width, h)
        c.restore()
        ink.colorFilter = null
    }

    fun cover(c: Canvas, width: Float, height: Float) {
        val s = min(width / 470f, height / 300f)
        c.save()
        c.scale(s, s)
        val w = width / s
        val h = height / s
        floor(c, w, h, 0f)
        ink.color = 0x2ac8ddff
        ink.style = Paint.Style.STROKE
        ink.strokeWidth = 1.5f
        c.drawCircle(w * .84f, h * .51f, 86f, ink)
        c.drawCircle(w * .84f, h * .51f, 98f, ink)
        ink.style = Paint.Style.FILL
        ink.color = -1
        val heroW = min(w * .52f, h * .85f)
        val x = w * .53f - heroW * .50f
        val base = h * .91f
        rider(c, x, base, heroW, 0f, false)
        val handX = x + heroW * .745f
        val handY = base - heroW * .43f
        sprite(c, ball, handX - 7, handY - 12, 24f, 24f)
        sprite(c, hoop, w * .79f, h * .39f, 84f, 75f)
        c.restore()
    }

    fun race(c: Canvas, width: Float, height: Float, r: Race) {
        val portrait = height >= width
        val viewportWidth = if (portrait) 450f else 690f
        val s = min(width / viewportWidth, height / 430f)
        c.save()
        c.scale(s, s)
        val w = width / s
        val h = height / s
        val hand = w * (if (portrait) .31f else .29f)
        val pixelsPerMeter = if (portrait) 7f else 12f
        val ground = h * .85f
        val phase = (r.elapsed * (9 + r.speed * .24)).toFloat()
        val handY = ground - 66f
        val rimY = handY - 75f
        fun trackX(m: Double) = hand + (m - r.distance).toFloat() * pixelsPerMeter
        floor(c, w, h, (r.distance * pixelsPerMeter).toFloat())
        val finishX = trackX(TRACK)
        if (finishX < w + 30) {
            for (row in 0..8) for (col in 0..1) {
                ink.color = if ((row + col) % 2 == 0) -1 else 0xff112655.toInt()
                val y = ground - 30 + row * 10
                c.drawRect(finishX + col * 10, y, finishX + (col + 1) * 10, y + 10, ink)
            }
        }
        for (i in 0..1) {
            val speed = 10 + i
            val x = trackX(min(TRACK, r.elapsed * speed))
            rider(c, x - 72, ground - 55 + i * 20, 94f + i * 15f, phase * .85f + i * 2, true)
        }
        var m = 35.0
        while (m < TRACK) {
            val x = trackX(m)
            if (x in -110f..(w + 60f)) {
                val pulse = if (r.call == Call.Basket && r.callTime > .62 && m == r.nextHoop - 55)
                    sin((1.05 - r.callTime) * 35).toFloat() * 3 else 0f
                if (m == r.nextHoop && r.inRange) {
                    ink.color = 0x6cffe289
                    c.drawOval(x - 44, rimY - 17, x + 44, rimY + 16, ink)
                }
                ink.color = -1
                sprite(c, hoop, x - 29, rimY - 37, 85f, 76f + pulse)
            }
            m += 55
        }
        if (r.boost > 0) {
            ink.strokeWidth = 3f
            for (i in 0..5) {
                ink.color = if (i % 2 == 0) 0xaaffca45.toInt() else 0x78e4ecff
                val x = hand - 110 - ((r.elapsed * 180 + i * 21) % 68).toFloat()
                c.drawLine(x - 25 - i * 2, ground - 10 - i * 6, x, ground - 10 - i * 6, ink)
            }
        }
        rider(c, hand - 117f, ground, 156f, phase, false)
        val shot = r.shot
        val bx = shot?.let { trackX(it.position) } ?: hand
        val by = if (shot != null) handY - shot.height.toFloat() else handY + sin(phase) * 2.6f
        ink.color = -1
        c.save()
        c.rotate((shot?.age?.times(370) ?: 0.0).toFloat(), bx, by)
        sprite(c, ball, bx - 12, by - 12, 24f, 24f)
        c.restore()
        c.restore()
    }
}

@Composable
internal fun arenaArt(): ArenaArt {
    val res = LocalContext.current.resources
    return remember(res) { ArenaArt(res) }
}

@Composable
internal fun Cover(art: ArenaArt) {
    ComposeCanvas(Modifier.fillMaxSize()) {
        art.cover(drawContext.canvas.nativeCanvas, size.width, size.height)
    }
}
