package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PreferencesManager(this)
        prefsManager.resetParentalLock()
        
        val activation = prefsManager.getActivationCode()
        if (activation == null || !activation.isActive) {
            startActivity(Intent(this, ActivationActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_main)
        
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        
        loadChannels()
    }
    
    private fun loadChannels() {
        val m3uUrl = "https://liveo-backend.onrender.com/api/playlist/${prefsManager.getActivationCode()?.code}"
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val channels = withContext(Dispatchers.IO) {
                    M3UParser.parseFromUrl(m3uUrl)
                }
                
                if (channels.isNotEmpty()) {
                    val adapter = ViewPagerAdapter(this@MainActivity, channels, prefsManager)
                    viewPager.adapter = adapter
                    tabLayout.setupWithViewPager(viewPager)
                } else {
                    Toast.makeText(this@MainActivity, "لا توجد قنوات متاحة", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطأ في التحميل: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
