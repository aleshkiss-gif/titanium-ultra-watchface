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
) : Renderer.CanvasRenderer(
    surfaceHolder,
    style,
    watchState,
    Renderer.CanvasType.HARDWARE,
    1000L,
    false
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val orange = Color.rgb(255, 112, 30)
    private val white = Color.rgb(245, 246, 247)
    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false) {
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.typeface = if (bold) Typeface.create("sans-serif-condensed", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL)
        paint.textSize = size
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(text, x, y, paint)
    }

    override fun render(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: Renderer.SharedAssets) {
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        canvas.drawColor(Color.BLACK)
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(w / 450f, h / 450f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.color = Color.rgb(60, 64, 69)
        canvas.drawCircle(225f, 225f, 204f, paint)
        paint.color = orange
        paint.strokeWidth = 9f
        canvas.drawArc(30f, 30f, 420f, 420f, -90f, 75f, false, paint)
        paint.style = Paint.Style.FILL
        drawText(canvas, "TITANIUM  /  ULTRA", 225f, 108f, 21f, orange, true)
        val time = zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        drawText(canvas, time, 225f, 252f, 112f, white, true)
        drawText(canvas, zonedDateTime.format(DateTimeFormatter.ofPattern("EEE  dd MMM")).uppercase(), 225f, 308f, 29f, white, true)
        drawText(canvas, "SEC  " + zonedDateTime.format(DateTimeFormatter.ofPattern("ss")), 225f, 365f, 23f, orange, true)
        canvas.restore()
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: Renderer.SharedAssets) {
        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
    }
}
