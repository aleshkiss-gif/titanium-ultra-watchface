package com.titanium.ultra

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TitaniumWatchFaceService : WatchFaceService() {
    override fun createUserStyleSchema() = UserStyleSchema(emptyList())

    override fun createComplicationSlotsManager(currentUserStyleRepository: CurrentUserStyleRepository): ComplicationSlotsManager =
        ComplicationSlotsManager(emptyList(), currentUserStyleRepository)

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: androidx.wear.watchface.WatchState,
        complicationSlotsManager: androidx.wear.watchface.ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = TitaniumRenderer(surfaceHolder, currentUserStyleRepository, watchState)
        return WatchFace(WatchFaceType.DIGITAL, renderer)
    }
}

private class TitaniumRenderer(
    surfaceHolder: SurfaceHolder,
    style: CurrentUserStyleRepository,
    watchState: androidx.wear.watchface.WatchState
) : Renderer.CanvasRenderer(surfaceHolder, style, watchState,
    androidx.wear.watchface.CanvasType.HARDWARE, 1000L, false) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val orange = Color.rgb(255, 108, 20)
    private val silver = Color.rgb(222, 226, 232)
    private val dim = Color.rgb(91, 98, 108)

    private fun circle(c: Canvas, x: Float, y: Float, r: Float, color: Int, stroke: Float = 0f) {
        p.color = color; p.style = if (stroke > 0) Paint.Style.STROKE else Paint.Style.FILL
        p.strokeWidth = stroke; c.drawCircle(x, y, r, p); p.style = Paint.Style.FILL
    }
    private fun txt(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = true) {
        p.color = color; p.style = Paint.Style.FILL; p.textSize = size
        p.typeface = Typeface.create("sans-serif-condensed", if (bold) Typeface.BOLD else Typeface.NORMAL)
        p.textAlign = Paint.Align.CENTER; c.drawText(s, x, y, p)
    }
    private fun line(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float) {
        p.color = color; p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
        p.strokeWidth = width; c.drawLine(x1, y1, x2, y2, p); p.style = Paint.Style.FILL
    }
    private fun gauge(c: Canvas, x: Float, y: Float, color: Int, label: String, value: String, small: String) {
        circle(c, x, y, 46f, Color.rgb(8, 10, 13))
        circle(c, x, y, 45f, Color.rgb(57, 62, 69), 3f)
        p.color = color; p.style = Paint.Style.STROKE; p.strokeWidth = 5f
        p.strokeCap = Paint.Cap.ROUND
        c.drawArc(x-40, y-40, x+40, y+40, -90f, 245f, false, p)
        p.style = Paint.Style.FILL
        txt(c, label, x, y-18, 17f, color)
        txt(c, value, x, y+13, 29f, silver)
        txt(c, small, x, y+32, 12f, dim)
    }
    private fun hand(c: Canvas, degrees: Float, length: Float, width: Float, color: Int) {
        c.save(); c.rotate(degrees, 225f, 225f)
        line(c, 225f, 238f, 225f, 225f-length, Color.BLACK, width+5f)
        line(c, 225f, 238f, 225f, 225f-length, color, width)
        c.restore()
    }
    override fun render(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
        canvas.drawColor(Color.BLACK)
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(bounds.width()/450f, bounds.height()/450f)
        // Dark titanium dial with concentric engraved rings
        circle(canvas,225f,225f,221f,Color.rgb(37,40,45))
        circle(canvas,225f,225f,215f,Color.rgb(142,146,151),3f)
        circle(canvas,225f,225f,206f,Color.rgb(8,10,13))
        circle(canvas,225f,225f,197f,Color.rgb(68,72,78),2f)
        for (i in 0 until 60) {
            canvas.save(); canvas.rotate(i*6f,225f,225f)
            val major = i%5==0
            line(canvas,225f, if(major)35f else 39f,225f,if(major)56f else 47f,
                if(major) orange else dim,if(major)5f else 1.7f)
            canvas.restore()
        }
        // Subdial surrounds and typography
        gauge(canvas,126f,152f,orange,"KCAL","--","ккал")
        gauge(canvas,324f,152f,orange,"STEPS","--","шагов")
        gauge(canvas,126f,292f,Color.rgb(44,222,95),"BAT","--","%")
        gauge(canvas,324f,292f,orange,"WEATHER","--","°C")
        txt(canvas,"TITANIUM",225f,102f,22f,silver)
        txt(canvas,"ULTRA",225f,124f,18f,orange)
        txt(canvas,"12",225f,73f,41f,silver)
        txt(canvas,"9",55f,239f,40f,silver)
        txt(canvas,"3",395f,239f,40f,silver)
        txt(canvas,"6",225f,417f,40f,silver)
        val locale = java.util.Locale("ru","RU")
        val date = zonedDateTime.format(DateTimeFormatter.ofPattern("EEE, dd MMM",locale)).uppercase(locale)
        txt(canvas,date,225f,343f,20f,orange)
        txt(canvas,zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),225f,388f,43f,silver)
        val second = zonedDateTime.second.toFloat()
        val minute = zonedDateTime.minute + second/60f
        val hour = (zonedDateTime.hour%12) + minute/60f
        hand(canvas,hour*30f,93f,13f,silver)
        hand(canvas,minute*6f,142f,9f,silver)
        hand(canvas,second*6f,168f,3f,orange)
        circle(canvas,225f,225f,10f,Color.BLACK)
        circle(canvas,225f,225f,6f,orange)
        canvas.restore()
    }
    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
    }
}
