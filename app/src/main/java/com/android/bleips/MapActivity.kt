package com.android.bleips

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.bleips.util.Calculator
import com.android.bleips.util.JsonParser
import com.beust.klaxon.JsonObject
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver
import com.lemmingapex.trilateration.TrilaterationFunction
import es.usc.citius.hipster.algorithm.Hipster
import es.usc.citius.hipster.graph.GraphBuilder
import es.usc.citius.hipster.graph.GraphSearchProblem
import es.usc.citius.hipster.graph.HipsterGraph
import kotlinx.android.synthetic.main.activity_map.*
import org.altbeacon.beacon.*
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer

class MapActivity : AppCompatActivity(), BeaconConsumer {

    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val TAG = "TestingBeacon"

    private lateinit var beaconManager: BeaconManager
    private lateinit var graph: HipsterGraph<Int?, Double?>

    private var nodeDestination = 0
    private var floorDestination = 0
    private var currentFloor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        nodeDestination = intent.getIntExtra("destination", -1)
        floorDestination = intent.getIntExtra("floor_dest", -1)
        currentFloor = intent.getIntExtra("current_floor", -1)

        verifyBluetooth()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("This app needs location access")
                builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_COARSE_LOCATION
                    )
                }
                builder.show()
            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.bind(this)

        recreateGraph()

        image_view.orientation = SubsamplingScaleImageView.ORIENTATION_90
        image_view.setImage(ImageSource.resource(R.drawable.floor_1))
    }

    override fun onBeaconServiceConnect() {
        val rangeNotifier = RangeNotifier { beacons, _ ->
            Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  ${beacons.size}")

            if (beacons.size >= 3) {
                val distances = DoubleArray(beacons.size)
                val positions = Array(beacons.size) { DoubleArray(2) }

                var nearestBeacon = beacons.first()

                for ((i, beacon: Beacon) in beacons.withIndex()) {
                    if (beacon.distance < nearestBeacon.distance)
                        nearestBeacon = beacon

                    val beaconObj = getBeaconData(beacon.id2.toInt(), beacon.id3.toInt())
                    distances[i] = beacon.distance
                    positions[i][0] = beaconObj.int("x")!!.toDouble()
                    positions[i][1] = beaconObj.int("y")!!.toDouble()
                }

                val solver = NonLinearLeastSquaresSolver(TrilaterationFunction(positions, distances), LevenbergMarquardtOptimizer())
                val optimum = solver.solve()

                val currentLocation = optimum.point.toArray()
                Log.i(TAG, "Current Location = ${currentLocation[0]} ${currentLocation[1]}")

                val nearestBeaconObj = getBeaconData(nearestBeacon.id2.toInt(), nearestBeacon.id3.toInt())
                if (nearestBeaconObj.int("room_code")!! > 0) {
                    drawRoute(Point(currentLocation[0].toInt(), currentLocation[1].toInt()),
                        nearestBeaconObj.int("room_code")!!
                    )
                }
                else {
                    drawRoute(Point(currentLocation[0].toInt(), currentLocation[1].toInt()),0)
                }
            }
        }

        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
            beaconManager.addRangeNotifier(rangeNotifier)
        } catch (e: RemoteException) { }
    }

    override fun onPause() {
        super.onPause()
        beaconManager.unbind(this)
    }

    override fun onResume() {
        super.onResume()
        beaconManager.bind(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {}
                    builder.show()
                }
            }
        }
    }

    private fun verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Bluetooth not enabled")
                builder.setMessage("Please enable bluetooth in settings and restart this application.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {}
                builder.show()
            }
        }
        catch (e: RuntimeException) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Bluetooth LE not available")
            builder.setMessage("Sorry, this device does not support Bluetooth LE.")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {}
            builder.show()
        }
    }

    private fun getBeaconData(major: Int, minor: Int) : JsonObject {
        val jsonString = StringBuilder(resources.openRawResource(R.raw.beacons)
            .bufferedReader().use { it.readText() })

        return JsonParser.getBeacon(jsonString, major, minor)
    }

    private fun drawRoute(currentLocation: Point, roomCode: Int) {
        var nodeOrigin = JsonObject()

        val jsonString = StringBuilder(resources.openRawResource(R.raw.nodes)
            .bufferedReader().use { it.readText() })

        if (roomCode > 0) {
            nodeOrigin = JsonParser.getNode(jsonString, roomCode)
        }
        else {
            val nodeList = JsonParser.getNodes(jsonString, currentFloor, 0)

            var nearestDistance = Double.MAX_VALUE

            nodeList.forEach {
                val distanceBetweenPoint = Calculator.calculateDistanceBetweenPoint(
                    currentLocation,
                    Point(it.int("x")!!, it.int("y")!!)
                )

                if (distanceBetweenPoint < nearestDistance) {
                    nearestDistance = distanceBetweenPoint
                    nodeOrigin = it
                }
            }
        }

        val graphProblem = GraphSearchProblem
            .startingFrom(nodeOrigin.int("room_code"))
            .`in`(graph)
            .takeCostsFromEdges()
            .build()

        val dijkstra = Hipster.createDijkstra(graphProblem).search(nodeDestination)

        val optimalPaths = dijkstra.optimalPaths

        var s = ""
        var c = ""

        val destinations = mutableListOf<PointF>()
        optimalPaths.forEach { l ->
            l.forEach { i ->
                s += "${i.toString()} "
                val node = JsonParser.getNode(jsonString, i!!)
                destinations.add(
                    PointF(
                        node.int("x")!!.toFloat() * 2,
                        node.int("y")!!.toFloat() * 2
                    ))

                c += "${node.int("x")} ${node.int("y")} "
            }
        }
        Log.i(TAG, s)
        Log.i(TAG, c)

        val origin = PointF(currentLocation.x.toFloat(), currentLocation.y.toFloat())
        image_view.drawPoints(origin, destinations)
    }

    private fun recreateGraph() {
        val g = GraphBuilder.create<Int, Double>()

        val nodeMappingString = StringBuilder(resources.openRawResource(R.raw.node_mapping)
            .bufferedReader().use { it.readText() })

        val nodesString = StringBuilder(resources.openRawResource(R.raw.nodes)
            .bufferedReader().use { it.readText() })

        val nodeMappingList = JsonParser.getNodeMappings(nodeMappingString, currentFloor)
        val nodesList = JsonParser.getNodes(nodesString, currentFloor)

        nodeMappingList.forEach {
            val from = JsonParser.getNodeFromArray(nodesList, it.int("from")!!)
            val to = JsonParser.getNodeFromArray(nodesList, it.int("to")!!)

            g.connect(from.int("room_code"))
                .to(to.int("room_code"))
                .withEdge(Calculator.calculateDistanceBetweenPoint(
                    Point(from.int("x")!!, from.int("y")!!),
                    Point(to.int("x")!!, to.int("y")!!)
                ))
        }

        graph = g.createUndirectedGraph()
    }

}