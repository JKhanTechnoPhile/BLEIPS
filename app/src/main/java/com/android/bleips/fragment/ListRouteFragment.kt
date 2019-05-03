package com.android.bleips.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.android.bleips.R
import com.android.bleips.activity.MapActivity
import com.android.bleips.util.DialogBuilder
import com.android.bleips.util.ResourceHelper
import com.github.loadingview.LoadingDialog
import kotlinx.android.synthetic.main.fragment_list_route.*
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region

class ListRouteFragment : Fragment(), BeaconConsumer {

    private val ARG_SECTION_NUMBER = "section_number"
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val TAG = "ListRouteFragment"

    private var sectionNumber = 0

    private lateinit var rootLayout: View
    private lateinit var floors:Array<String>
    private lateinit var mContext: Context
    private lateinit var beaconManager: BeaconManager
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var mapIntent: Intent

    companion object {
        fun newInstance(sectionNumber: Int, listRouteFragment: ListRouteFragment) : ListRouteFragment {
            val fragment = ListRouteFragment()
            val args = Bundle()
            args.putInt(listRouteFragment.ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sectionNumber = arguments!!.getInt(ARG_SECTION_NUMBER, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootLayout = inflater.inflate(R.layout.fragment_list_route, container, false)
        mContext = rootLayout.context

        val listFloors = rootLayout.findViewById(R.id.list_floors) as ListView

        loadingDialog = LoadingDialog[this.activity!!]

        beaconManager = BeaconManager.getInstanceForApplication(mContext)

        return rootLayout
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        beaconManager.unbind(this)
    }

    override fun onBeaconServiceConnect() {
        val rangeNotifier = RangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                var nearestBeacon = beacons.first()
                beacons.forEach {
                    if (it.distance < nearestBeacon.distance)
                        nearestBeacon = it
                }

                loadingDialog.hide()

                beaconManager.unbind(this)

                mapIntent.putExtra("current_floor", nearestBeacon.id2.toInt())
                startActivity(mapIntent)
            }
        }

        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
            beaconManager.addRangeNotifier(rangeNotifier)
        } catch (e: RemoteException) { }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted")
                } else {
                    val builder = DialogBuilder.createDialog(
                        mContext,
                        "error_location_permission_missing",
                        0
                    ).show()
                }
            }
        }
    }

    override fun getApplicationContext(): Context {
        return mContext
    }

    override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean {
        return true
    }

    override fun unbindService(p0: ServiceConnection?) { }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        floors = resources.getStringArray(
            ResourceHelper.getResourceId(mContext, "f$sectionNumber", "array")
        )

        list_floors.adapter = ArrayAdapter(mContext, android.R.layout.simple_list_item_1, floors)

        list_floors.setOnItemClickListener { parent, view, position, id ->
            openDialog(position)
        }
    }

    private fun openDialog(position: Int) {
        val builder = AlertDialog.Builder(rootLayout.context)
        builder.setMessage("Make route to ${floors[position]}?")
            .setPositiveButton(R.string.dialog_yes) { dialog, which ->
                if (isBluetoothVerified()) {
                    mapIntent = Intent(rootLayout.context, MapActivity::class.java)
                    mapIntent.putExtra("destination", position + 1)
                    mapIntent.putExtra("floor_dest", sectionNumber)

                    loadingDialog.show()

                    beaconManager.bind(this)
                }
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, which ->

            }
        builder.create()

        builder.show()
    }

    private fun isBluetoothVerified() : Boolean {
        try {
            if (!BeaconManager.getInstanceForApplication(mContext).checkAvailability()) {
                val builder = DialogBuilder.createDialog(
                    mContext,
                    "error_bluetooth_disabled",
                    0
                ).show()
                return false
            }
        }
        catch (e: RuntimeException) {
            val builder = DialogBuilder.createDialog(
                mContext,
                "error_bluetooth_not_available",
                0
            ).show()
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                val builder = DialogBuilder.createDialog(
                    mContext,
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
                return false
            }
        }

        return true
    }

}