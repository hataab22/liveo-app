package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
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
            
            // ✅ قنوات تجريبية بدون Network
            val testChannels = listOf(
                Channel("1", "القناة 1", "https://test.com/ch1.m3u8", "", "عام"),
                Channel("2", "القناة 2", "https://test.com/ch2.m3u8", "", "عام"),
                Channel("3", "القناة 3", "https://test.com/ch3.m3u8", "", "رياضة")
            )
            
            Toast.makeText(this, "تم تحميل ${testChannels.size} قنوات", Toast.LENGTH_SHORT).show()
            
            val adapter = ViewPagerAdapter(supportFragmentManager, testChannels, prefsManager)
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)
            
        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
