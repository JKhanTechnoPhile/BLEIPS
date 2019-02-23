package com.android.bleips

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.android.bleips.util.CanvasDrawer
import com.bumptech.glide.Glide
import com.glidebitmappool.GlideBitmapFactory
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() {

    private lateinit var canvasDrawer: CanvasDrawer
    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap

    private var curX = 1000f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        canvasDrawer = CanvasDrawer(this)
        bitmap = GlideBitmapFactory.decodeResource(resources, R.drawable.floor_1)
        canvas = Canvas(bitmap)
        canvasDrawer.draw(canvas)

        updateImageView()

        btn_update.setOnClickListener {
            curX += 100
            canvasDrawer.setCurX(curX)
            updateImageView()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateImageView() {
        Glide.with(this).load(bitmap).into(photo_view)
    }
}