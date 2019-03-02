package com.android.bleips.fragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.android.bleips.MapActivity
import com.android.bleips.R
import kotlinx.android.synthetic.main.fragment_floor_one.*

class FloorOneFragment : Fragment() {

    private lateinit var rootLayout:View
    private lateinit var floors:Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootLayout = inflater.inflate(R.layout.fragment_floor_one, container, false)

        return rootLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        floors = resources.getStringArray(R.array.floor_one)
        list_floors.adapter = ArrayAdapter(rootLayout.context, android.R.layout.simple_list_item_1, floors)

        list_floors.setOnItemClickListener { parent, view, position, id ->
            openDialog(position)
        }
    }

    private fun openDialog(position: Int) {
        val builder = AlertDialog.Builder(rootLayout.context)
        builder.setMessage("Make route to ${floors[position]}?")
            .setPositiveButton(R.string.dialog_yes
            ) { dialog, which ->
                val intent = Intent(rootLayout.context, MapActivity::class.java).apply {
                    putExtra("destination", position + 1)
                    putExtra("floor_dest", 1)
                    putExtra("current_floor", 1)
                }
                startActivity(intent)
            }
            .setNegativeButton(R.string.dialog_cancel
            ) { dialog, which ->

            }
        builder.create().show()
    }

}
