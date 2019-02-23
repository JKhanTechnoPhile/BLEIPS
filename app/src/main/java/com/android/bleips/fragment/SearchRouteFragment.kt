package com.android.bleips.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.bleips.R
import com.android.bleips.adapter.RouteFragmentPagerAdapter
import kotlinx.android.synthetic.main.fragment_search_route.view.*

class SearchRouteFragment : Fragment() {

    lateinit var rootLayout:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootLayout = inflater.inflate(R.layout.fragment_search_route, container, false)

        rootLayout.viewpager_main.adapter = RouteFragmentPagerAdapter(childFragmentManager)

        return rootLayout
    }
}
