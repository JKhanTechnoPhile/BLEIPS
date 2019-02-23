package com.android.bleips.fragment


import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.bleips.R
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.android.synthetic.main.fragment_map.*

class MapFragment : Fragment() {

    lateinit var rootLayout:View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootLayout = inflater.inflate(R.layout.fragment_map, container, false)

        return rootLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image_view.orientation = SubsamplingScaleImageView.ORIENTATION_90
        image_view.setImage(ImageSource.resource(R.drawable.floor_1))

        val destinations: MutableList<PointF> = mutableListOf(PointF(3000f, 1880f), PointF(2900f, 1880f), PointF(2800f, 1700f))
        val origin = PointF(3000f, 1700f)
        image_view.drawPoints(origin, destinations)
    }

}
