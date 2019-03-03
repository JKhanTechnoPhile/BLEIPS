package com.android.bleips.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.android.bleips.R
import com.android.bleips.fragment.AboutFragment
import com.android.bleips.fragment.SearchRouteFragment
import com.android.bleips.fragment.TestingFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Base)
        super.onCreate(savedInstanceState)

        if (!isTaskRoot) {
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.label_search_route)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
        }

        mDrawerLayout = findViewById(R.id.drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            // update the UI based on the item selected
            var selectedFragment = Fragment()
            when (menuItem.itemId) {
                R.id.nav_drawer_search_route -> {
                    selectedFragment = SearchRouteFragment()
                    supportActionBar?.setTitle(R.string.label_search_route)
                }
                R.id.nav_drawer_testing -> {
                    selectedFragment = TestingFragment()
                    supportActionBar?.setTitle(R.string.label_testing)
                }
                R.id.nav_drawer_about -> {
                    selectedFragment = AboutFragment()
                    supportActionBar?.setTitle(R.string.label_about)
                }
            }

            supportFragmentManager.beginTransaction().replace(R.id.content_frame, selectedFragment).commit()

            true
        }

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction().add(
                R.id.content_frame,
                SearchRouteFragment()
            ).commit()
        }

    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
