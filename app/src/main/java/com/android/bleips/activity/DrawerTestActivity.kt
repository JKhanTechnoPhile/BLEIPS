package com.android.bleips.activity

import android.graphics.PointF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.bleips.R
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_drawer_test.*

class DrawerTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_test)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        draw()
    }

    private fun draw() {
        image_view.setImage(ImageSource.resource(R.drawable.f1))
        image_view.setPin(PointF(565f, 600f))
    }
}
