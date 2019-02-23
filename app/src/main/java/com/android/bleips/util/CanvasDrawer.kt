package com.android.bleips.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class CanvasDrawer(context: Context) : View(context) {

    private var mPaint = Paint()
    private var curX = 0f

    init {
        mPaint.apply {
            color = Color.BLUE
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8f
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawLine(0f, 100f, curX, 100f, mPaint)
    }

    fun setCurX(x: Float) {
        curX = x
        invalidate()
    }

}