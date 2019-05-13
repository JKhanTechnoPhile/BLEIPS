package com.android.bleips.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class DrawView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs) {

    private var strokeWidth: Int = 0

    private val MAP_IMAGE_WIDTH = 1500
    private val MAP_IMAGE_HEIGHT = 942

    private val paint = Paint()
    private val path = Path()
    private var sOrigin: PointF? = null

    private var imageScale = PointF()

    private var destinationList: MutableList<PointF>? = null

    init {
        initialise()
    }

    private fun initialise() {
        val density = resources.displayMetrics.densityDpi.toFloat()
        strokeWidth = (density / 40f).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isReady) return

        imageScale = getImageScale()

        path.reset()

        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = strokeWidth.toFloat()
        paint.color = Color.argb(255, 38, 166, 154)

        if (sOrigin != null) {
            val vOrigin = sourceToViewCoord(processPoint(sOrigin!!))
            path.moveTo(vOrigin!!.x, vOrigin.y)

            Log.d("TestingBeacon", "sOrigin = ${sOrigin!!.x} ${sOrigin!!.y}")
            Log.d("TestingBeacon", "vOrigin = ${vOrigin.x} ${vOrigin.y}")

            destinationList?.forEach {
                Log.d("TestingBeacon", "sHeight = $sHeight")
                Log.d("TestingBeacon", "it = ${it.x} ${it.y}")
                val vDest = sourceToViewCoord(processPoint(it))
                path.lineTo(vDest!!.x, vDest.y)
                Log.d("TestingBeacon", "vDest = ${vDest.x} ${vDest.y}")
            }
            canvas.drawPath(path, paint)
        }
    }

    fun drawPoints(origin: PointF, destinations: MutableList<PointF>) {
        sOrigin = origin
        destinationList = destinations

        invalidate()
    }

    fun clearCanvas() {
        path.reset()
        sOrigin = null
        destinationList = null
        invalidate()
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