package com.android.bleips.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.bleips.R
import com.android.bleips.util.DialogBuilder
import kotlinx.android.synthetic.main.activity_range_test.*
import org.altbeacon.beacon.*
import java.text.SimpleDateFormat
import java.util.*

class RangeTestActivity : AppCompatActivity(), BeaconConsumer {

    private val TAG = "TestingBeacon"
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private var logString = ""

    private lateinit var beaconManager: BeaconManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_range_test)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        beaconManager = BeaconManager.getInstanceForApplication(this)

        btn_test_start.setOnClickListener {
            verifyBluetooth()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android M Permission check
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    val builder = DialogBuilder.createDialog(
                        this,
                        "error_location_services_disabled",
                        1
                    )
                    builder.setOnDismissListener {
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            PERMISSION_REQUEST_COARSE_LOCATION
                        )
                    }
                    builder.show()
                }
            }

            beaconManager.bind(this)
        }

        btn_test_stop.setOnClickListener {
            beaconManager.unbind(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }

    override fun onBeaconServiceConnect() {
        val rangeNotifier = RangeNotifier { beacons, _ ->
            Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  ${beacons.size}")

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val currentDate = dateFormat.format(calendar.time)

            for ((i, beacon: Beacon) in beacons.withIndex()) {
                val newLine = "" +
                        "$currentDate\n" +
                        "${beacon.bluetoothAddress}\n" +
                        "Major: ${beacon.id2} Minor: ${beacon.id3} TxPower: ${beacon.txPower}\n" +
                        "RSSI: ${beacon.rssi} Distance: ${beacon.distance}" +
                        "\n\n"

                logString += newLine

                text_log.text = logString
                scrollDown()
//                scroll_view.fullScroll(View.FOCUS_DOWN)
            }
        }

        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
            beaconManager.removeAllRangeNotifiers()
            beaconManager.addRangeNotifier(rangeNotifier)
        } catch (e: RemoteException) { }
    }

    private fun verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                val builder = DialogBuilder.createDialog(
                    this,
                    "error_bluetooth_disabled",
                    0
                ).show()
            }
        }
        catch (e: RuntimeException) {
            val builder = DialogBuilder.createDialog(
                this,
                "error_bluetooth_not_available",
                0
            ).show()
        }
    }

    private fun scrollDown() {
        Handler().postDelayed(
            {
                scroll_view.fullScroll(View.FOCUS_DOWN)
            }, 200
        )

//        Runnable { scroll_view.fullScroll(View.FOCUS_DOWN) }
    }
}
