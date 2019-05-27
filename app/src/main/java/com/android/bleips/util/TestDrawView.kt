package com.android.bleips.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.android.bleips.R
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class TestDrawView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs) {

    private val paint = Paint()
    private val vPin = PointF()
    private var sPin: PointF? = null
    private var pin: Bitmap

    init {
        val density = resources.displayMetrics.densityDpi.toFloat()
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_place_blue_24dp)
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

        val bCanvas = Canvas(bitmap)
        drawable.setBounds(0, 0, bCanvas.width, bCanvas.height)
        drawable.draw(bCanvas)

        val w = density / 420f * bitmap.width
        val h = density / 420f * bitmap.height
        pin = Bitmap.createScaledBitmap(bitmap, w.toInt(), h.toInt(), true)
    }

    fun setPin(sPin: PointF) {
        this.sPin = sPin
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady) return

        paint.isAntiAlias = true

        if (sPin != null) {
            sourceToViewCoord(sPin, vPin)
            val vX = vPin.x - pin.width / 2
            val vY = vPin.y - pin.height
            canvas.drawBitmap(pin, vX, vY, paint)
        }

    }

}