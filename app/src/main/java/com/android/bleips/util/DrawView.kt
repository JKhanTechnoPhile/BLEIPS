package com.android.bleips.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.core.content.ContextCompat
import com.android.bleips.R
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class DrawView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SubsamplingScaleImageView(context, attrs) {

    private var strokeWidth: Int = 0
    private lateinit var pinOrigin: Bitmap
    private lateinit var pinDestination: Bitmap
    private var destinationSize: Int = 0

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

        val drawablePinOrigin = ContextCompat.getDrawable(context, R.drawable.ic_person_pin_blue_24dp)
        val bitmapPinOrigin = Bitmap.createBitmap(
            drawablePinOrigin!!.intrinsicWidth,
            drawablePinOrigin.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val drawablePinDestination = ContextCompat.getDrawable(context, R.drawable.ic_place_blue_24dp)
        val bitmapPinDestination = Bitmap.createBitmap(
            drawablePinDestination!!.intrinsicWidth,
            drawablePinDestination.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvasPinOrigin = Canvas(bitmapPinOrigin)
        drawablePinOrigin.setBounds(0, 0, canvasPinOrigin.width, canvasPinOrigin.height)
        drawablePinOrigin.draw(canvasPinOrigin)

        val canvasPinDestination = Canvas(bitmapPinDestination)
        drawablePinDestination.setBounds(0, 0, canvasPinDestination.width, canvasPinDestination.height)
        drawablePinDestination.draw(canvasPinDestination)

        val w = density / 420f * bitmapPinOrigin.width
        val h = density / 420f * bitmapPinOrigin.height

        val pinOriginUnrotated = Bitmap.createScaledBitmap(bitmapPinOrigin, w.toInt(), h.toInt(), true)
        val pinDestinationUnrotated = Bitmap.createScaledBitmap(bitmapPinDestination, w.toInt(), h.toInt(), true)

        val mMatrix = Matrix()
        mMatrix.postRotate(90F)

        pinOrigin = Bitmap.createBitmap(
            pinOriginUnrotated,
            0,
            0,
            pinOriginUnrotated.width,
            pinOriginUnrotated.height,
            mMatrix,
            true
        )
        pinDestination = Bitmap.createBitmap(
            pinDestinationUnrotated,
            0,
            0,
            pinDestinationUnrotated.width,
            pinDestinationUnrotated.height,
            mMatrix,
            true
        )
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
        paint.color = Color.argb(255, 84, 117, 158)

        if (sOrigin != null) {
            val vOrigin = sourceToViewCoord(processPoint(sOrigin!!))
            path.moveTo(vOrigin!!.x, vOrigin.y)
            canvas.drawBitmap(
                pinOrigin,
                (vOrigin.x - pinOrigin.width / 2),
                (vOrigin.y - pinOrigin.height),
                paint
            )

            Log.d("TestingBeacon", "sOrigin = ${sOrigin!!.x} ${sOrigin!!.y}")
            Log.d("TestingBeacon", "vOrigin = ${vOrigin.x} ${vOrigin.y}")

            destinationList?.forEachIndexed { index, pointF ->
                Log.d("TestingBeacon", "sHeight = $sHeight")
                Log.d("TestingBeacon", "it = ${pointF.x} ${pointF.y}")
                val vDest = sourceToViewCoord(processPoint(pointF))
                path.lineTo(vDest!!.x, vDest.y)
                Log.d("TestingBeacon", "vDest = ${vDest.x} ${vDest.y}")

                if (index == destinationSize - 1) {
                    canvas.drawBitmap(
                        pinDestination,
                        (vDest.x - pinDestination.width / 2),
                        (vDest.y - pinDestination.height),
                        paint
                    )
                }
            }
            canvas.drawPath(path, paint)
        }
    }

    fun drawPoints(origin: PointF, destinations: MutableList<PointF>) {
        sOrigin = origin
        destinationList = destinations
        destinationSize = destinations.size
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