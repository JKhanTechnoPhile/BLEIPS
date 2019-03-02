package com.android.bleips.util

import android.graphics.Point
import kotlin.math.sqrt

class Calculator {
    companion object {

        fun calculateDistanceBetweenPoint(from: Point, to: Point) : Double {
            return sqrt(Math.pow((to.x - from.x).toDouble(), 2.0) + Math.pow((to.y - from.y).toDouble(), 2.0))
        }

    }
}