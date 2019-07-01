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
import com.android.bleips.util.JsonParser
import com.beust.klaxon.JsonObject
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver
import com.lemmingapex.trilateration.TrilaterationFunction
import kotlinx.android.synthetic.main.activity_location_test.*
import org.altbeacon.beacon.*
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LocationTestActivity : AppCompatActivity(), BeaconConsumer {

    private val TAG = "TestingBeacon"
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1

    private lateinit var beaconManager: BeaconManager

    private var logString = ""
    private var logLocationString = ""
    private val listLocation: ArrayList<DoubleArray> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_test)

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

                    val fileName = "BLEIPS_LOCATION_$currentDate.txt"
                    val file = File(getExternalFilesDir(null), fileName)

                    try {
                        file.createNewFile()
                        val outputStream = FileOutputStream(file, true)
                        outputStream.write(logLocationString.toByteArray())
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

    override fun onBeaconServiceConnect() {
        val rangeNotifier = RangeNotifier { beacons, _ ->
            Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  ${beacons.size}")

            var currentFloor = -1

            if (beacons.isNotEmpty()) {
                var nearestBeacon = beacons.first()
                beacons.forEach {
                    if (it.distance < nearestBeacon.distance)
                        nearestBeacon = it
                }
                currentFloor = nearestBeacon.id2.toInt()
            }

            if (beacons.size >= 3) {
                val distances = DoubleArray(beacons.size)
                val positions = Array(beacons.size) { DoubleArray(2) }

                for ((i, beacon: Beacon) in beacons.withIndex()) {
                    if (beacon.id2.toInt() != currentFloor)
                        continue

                    val beaconObj = getBeaconData(beacon.id2.toInt(), beacon.id3.toInt())
                    distances[i] = beacon.distance
                    positions[i][0] = beaconObj.int("x")!!.toDouble()
                    positions[i][1] = beaconObj.int("y")!!.toDouble()
                }

                if (distances.size >= 3) {
                    val solver = NonLinearLeastSquaresSolver(TrilaterationFunction(positions, distances), LevenbergMarquardtOptimizer())
                    val optimum = solver.solve()

                    val currentLocation = optimum.point.toArray()
                    Log.i(TAG, "Current Location = ${currentLocation[0]} ${currentLocation[1]}")

                    printData(currentFloor, beacons.size, currentLocation)
                }
            }
            else {
                val currentLocation = doubleArrayOf()
                printData(currentFloor, beacons.size, currentLocation)
            }
        }

        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
            beaconManager.removeAllRangeNotifiers()
            beaconManager.addRangeNotifier(rangeNotifier)
        } catch (e: RemoteException) { }
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
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

    private fun printData(currentFloor: Int, beaconSize: Int, currentLocation: DoubleArray) {
        logString += if (currentLocation.isNotEmpty()) {
            "" +
                    "Current floor: $currentFloor\n" +
                    "Beacon in range: $beaconSize\n" +
                    "Current Location: ${currentLocation[0]} ${currentLocation[1]}" +
                    "\n\n"
        } else {
            "" +
                    "Current floor: $currentFloor\n" +
                    "Beacon in range: $beaconSize\n" +
                    "Cannot determine current location, beacon in range less than 3"+
                    "\n\n"
        }

        if ( currentLocation.isNotEmpty())
            logLocationString += "${currentLocation[0]} ${currentLocation[1]}\n"

        listLocation.add(currentLocation)
        text_log.text = logString
        scrollDown()
    }

    private fun getBeaconData(major: Int, minor: Int) : JsonObject {
        val jsonString = StringBuilder(resources.openRawResource(R.raw.beacons)
            .bufferedReader().use { it.readText() })

        return JsonParser.getBeacon(jsonString, major, minor)
    }

    private fun clearData() {
        logString = ""
        listLocation.clear()
        text_log.text = ""
    }
}
