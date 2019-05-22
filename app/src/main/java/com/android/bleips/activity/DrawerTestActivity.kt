package com.android.bleips.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.bleips.R

class DrawerTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_test)

        actionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
