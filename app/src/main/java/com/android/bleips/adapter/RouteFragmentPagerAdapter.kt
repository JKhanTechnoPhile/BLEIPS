package com.android.bleips.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.android.bleips.fragment.ListRouteFragment

class RouteFragmentPagerAdapter(fm: FragmentManager, noOfItems: Int): FragmentStatePagerAdapter(fm) {

    private var noOfItems = 0

    init {
        this.noOfItems = noOfItems
    }

    override fun getItem(position: Int) : Fragment {
        return ListRouteFragment.newInstance(position + 1, ListRouteFragment())
    }

    override fun getCount() : Int {
        return noOfItems
    }

    override fun getPageTitle(position: Int) : CharSequence {
        return "FLOOR "+ (position + 1)
    }
}