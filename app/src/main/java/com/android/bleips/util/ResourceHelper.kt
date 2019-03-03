package com.android.bleips.util

import android.content.Context

class ResourceHelper {
    companion object {

        fun getResourceId(context: Context, name: String, defType: String) : Int {
            return context.resources.getIdentifier(name, defType, context.packageName)
        }

    }
}