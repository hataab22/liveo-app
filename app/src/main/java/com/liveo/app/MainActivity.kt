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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activation = prefsManager.getActivationCode() ?: return@launch
                val response = ApiClient.verifyActivation(activation.code)
                
                if (response.success && response.m3u_url != null) {
                    val channels = M3UParser.parseFromUrl(response.m3u_url)
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        if (channels.isNotEmpty()) {
                            val adapter = ViewPagerAdapter(supportFragmentManager, channels, prefsManager)
                            viewPager.adapter = adapter
                            tabLayout.setupWithViewPager(viewPager)
                        } else {
                            Toast.makeText(this@MainActivity, "لا توجد قنوات", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, "انتهت الصلاحية", Toast.LENGTH_SHORT).show()
                        prefsManager.clearActivationCode()
                        startActivity(Intent(this@MainActivity, ActivationActivity::class.java))
                        finish()
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@MainActivity, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
