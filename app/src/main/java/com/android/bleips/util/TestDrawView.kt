package com.android.bleips.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Style
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class TestDrawView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs) {

    private var strokeWidth: Int = 0

    private val MAP_IMAGE_WIDTH = 1500
    private val MAP_IMAGE_HEIGHT = 942

    private val sCenter = PointF()
    private val vCenter = PointF()
    private val paint = Paint()
    private var imageScale = PointF()
    private val drawPoint = PointF(750F, 471F)

    init {
        initialise()
    }

    private fun initialise() {
        val density = resources.displayMetrics.densityDpi.toFloat()
        strokeWidth = (density / 60f).toInt()
        Log.d("TestDrawView", "Density: $density")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady) return

        imageScale = getImageScale()
        Log.d("TestDrawView", "ImageScale: ${imageScale.x} ${imageScale.y}")
        Log.d("TestDrawView", "DrawPoint: ${drawPoint.x} ${drawPoint.y}")

        val processedPoint = processPoint(drawPoint)

        sCenter.set(processedPoint)

        sourceToViewCoord(sCenter, vCenter)
        val radius = 2f

        Log.d("TestDrawView", "Height: $height Width: $width")
        Log.d("TestDrawView", "sHeight: $sHeight sWidth: $sWidth")

        Log.d("TestDrawView", "sCenter: ${sCenter.x}, ${sCenter.y}")
        Log.d("TestDrawView", "vCenter: ${vCenter.x}, ${vCenter.y}")


        paint.isAntiAlias = true
        paint.style = Style.STROKE
        paint.strokeCap = Cap.ROUND
        paint.strokeWidth = (strokeWidth * 2).toFloat()
        paint.color = Color.BLACK
        canvas.drawCircle(vCenter.x, vCenter.y, radius, paint)
        paint.strokeWidth = strokeWidth.toFloat()
        paint.color = Color.argb(255, 38, 166, 154)
        canvas.drawCircle(vCenter.x, vCenter.y, radius, paint)
    }

    private fun processPoint(pointF: PointF): PointF {
        val scaledPoint = scalePoint(pointF)
        Log.d("TestDrawView", "scaledPoint: ${scaledPoint.x} ${scaledPoint.y}")
        val rotatedPoint = rotatePoint(scaledPoint)
        Log.d("TestDrawView", "rotatedPoint: ${rotatedPoint.x} ${rotatedPoint.y}")

        return rotatedPoint
    }

    private fun rotatePoint(pointF: PointF): PointF {
        return PointF(sHeight - pointF.y, pointF.x)
    }

    private fun scalePoint(pointF: PointF): PointF {
        return PointF(pointF.x * imageScale.x, pointF.y * imageScale.y)
    }

    private fun getImageScale(): PointF {
        return PointF((sWidth.toFloat() / MAP_IMAGE_WIDTH), (sHeight.toFloat() / MAP_IMAGE_HEIGHT))
    }

}