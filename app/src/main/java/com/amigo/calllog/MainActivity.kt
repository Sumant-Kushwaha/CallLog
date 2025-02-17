package com.amigo.calllog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val REQUEST_READ_CALL_LOG = 123
    private val REQUEST_READ_SMS = 124

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        if (checkCallLogPermission() && checkSmsPermission()) {
            setupViewPager()
        } else {
            requestPermissions()
        }
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Missed"
                1 -> "Received"
                2 -> "Dialed"
                3 -> "Messages"
                else -> throw IllegalArgumentException()
            }
        }.attach()
    }

    private fun checkCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (!checkCallLogPermission()) {
            permissionsToRequest.add(Manifest.permission.READ_CALL_LOG)
        }
        
        if (!checkSmsPermission()) {
            permissionsToRequest.add(Manifest.permission.READ_SMS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_READ_CALL_LOG
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_READ_CALL_LOG) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setupViewPager()
            }
        }
    }
}