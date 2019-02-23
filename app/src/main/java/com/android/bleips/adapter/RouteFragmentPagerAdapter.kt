package com.android.bleips.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.android.bleips.fragment.FloorOneFragment
import com.android.bleips.fragment.FloorThreeFragment
import com.android.bleips.fragment.FloorTwoFragment

class RouteFragmentPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm){

    // sebuah list yang menampung objek Fragment
    private val pages = listOf(
        FloorOneFragment(),
        FloorTwoFragment(),
        FloorThreeFragment()
    )

    // menentukan fragment yang akan dibuka pada posisi tertentu
    override fun getItem(position: Int): Fragment {
        return pages[position]
    }

    override fun getCount(): Int {
        return pages.size
    }

    // judul untuk tabs
    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "Lantai 1"
            1 -> "Lantai 2"
            else -> "Lantai 3"
        }
    }
}