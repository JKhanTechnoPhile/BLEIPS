package com.android.bleips.util

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.android.bleips.R

class DialogBuilder {
    companion object {

        private val PERMISSION_REQUEST_COARSE_LOCATION = 1
        private val ERROR_MESSAGES = mapOf(
            "error_bluetooth_not_available" to R.string.error_bluetooth_not_available,
            "error_bluetooth_disabled" to R.string.error_bluetooth_disabled,
            "error_location_permission_missing" to R.string.error_location_permission_missing,
            "error_location_services_disabled" to R.string.error_location_services_disabled
        )

        fun createDialog(context: Context, error: String, request: Int) : AlertDialog.Builder {
            val message = context.getString(ERROR_MESSAGES.getValue(error)).split("$$").toTypedArray()

            Log.i("DialogBuilder", context.getString(ERROR_MESSAGES.getValue(error)))

            val builder = AlertDialog.Builder(context)
            builder.setTitle(message[0])
            builder.setMessage(message[1])
            builder.setPositiveButton(android.R.string.ok, null)

            if (request == 0) {
                builder.setOnDismissListener {}
            }

            return builder
        }

    }
}