package com.android.bleips.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class DrawView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs) {

    private var strokeWidth: Int = 0

    private val paint = Paint()
    private val path = Path()
    private var sOrigin: PointF? = null
    private val vOrigin = PointF()

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

        path.reset()

        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = strokeWidth.toFloat()
        paint.color = Color.argb(255, 38, 166, 154)

        if (sOrigin != null) {
            sourceToViewCoord(rotatePoint(sOrigin!!, sHeight), vOrigin)
            vOrigin.set(vOrigin)
            path.moveTo(vOrigin.x, vOrigin.y)

            destinationList?.forEach {
                val vDest = sourceToViewCoord(rotatePoint(it, sHeight))
                path.lineTo(vDest!!.x, vDest.y)
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun rotatePoint(pointF: PointF, size: Int): PointF {
        return PointF(size - pointF.y, pointF.x)
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

}