package com.android.bleips.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.bleips.R
import com.android.bleips.util.DialogBuilder
import kotlinx.android.synthetic.main.activity_range_test.*
import org.altbeacon.beacon.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RangeTestActivity : AppCompatActivity(), BeaconConsumer {

    private val TAG = "TestingBeacon"
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private var logString = ""
    private var logRssiString = ""
    private val listRssi: ArrayList<Int> = ArrayList()

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
            getResult()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_testing_range, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.clear -> {
                clearData()
                true
            }
            R.id.export -> {
                val state = Environment.getExternalStorageState()
                if (Environment.MEDIA_MOUNTED == state) {
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault())
                    val currentDate = dateFormat.format(calendar.time)

                    val fileName = "BLEIPS_$currentDate.txt"
                    val file = File(getExternalFilesDir(null), fileName)

                    try {
                        file.createNewFile()
                        val outputStream = FileOutputStream(file, true)
                        outputStream.write(logRssiString.toByteArray())
                        outputStream.flush()
                        outputStream.close()

                        Toast.makeText(this, "$fileName created", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
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

                listRssi.add(beacon.rssi)
                logString += newLine
                logRssiString += "${beacon.rssi}\n"

                text_log.text = logString
                scrollDown()
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
    }

    private fun getResult() {
        val newLine = "Average RSSI: ${listRssi.average()}\n\n"

        logString += newLine
        text_log.text = logString
        scrollDown()

        listRssi.clear()
    }

    private fun clearData() {
        logString = ""
        logRssiString = ""
        text_log.text = ""
        listRssi.clear()
    }
}
