package com.android.bleips.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.bleips.R
import com.android.bleips.activity.DrawerTestActivity
import com.android.bleips.activity.LocationTestActivity
import com.android.bleips.activity.RangeTestActivity
import kotlinx.android.synthetic.main.fragment_testing.view.*

class TestingFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_testing, container, false)
        mContext = rootView.context

        rootView.btn_test_range.setOnClickListener {
            val intent = Intent(mContext, RangeTestActivity::class.java)
            startActivity(intent)
        }

        rootView.btn_test_multiple.setOnClickListener {
            val intent = Intent(mContext, DrawerTestActivity::class.java)
            startActivity(intent)
        }

        rootView.btn_test_location.setOnClickListener {
            val intent = Intent(mContext, LocationTestActivity::class.java)
            startActivity(intent)
        }

        return rootView
    }

}
