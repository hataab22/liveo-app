package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var prefsManager: PreferencesManager
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "onCreate started")
            
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
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun loadChannels() {
        Toast.makeText(this, "جاري التحميل...", Toast.LENGTH_SHORT).show()
        
        val m3uUrl = "https://liveo-backend.onrender.com/api/playlist/${prefsManager.getActivationCode()?.code}"
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Loading from: $m3uUrl")
                
                val channels = withTimeout(45000L) { // 45 seconds
                    withContext(Dispatchers.IO) {
                        M3UParser.parseFromUrl(m3uUrl)
                    }
                }
                
                Log.d(TAG, "Loaded ${channels.size} channels")
                
                if (channels.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "تم تحميل ${channels.size} قناة بنجاح", Toast.LENGTH_SHORT).show()
                    
                    val adapter = ViewPagerAdapter(supportFragmentManager, channels, prefsManager)
                    viewPager.adapter = adapter
                    tabLayout.setupWithViewPager(viewPager)
                } else {
                    Toast.makeText(this@MainActivity, "لم يتم العثور على قنوات", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "Timeout loading channels", e)
                Toast.makeText(this@MainActivity, "انتهت مهلة التحميل. حاول مرة أخرى", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading channels", e)
                Toast.makeText(this@MainActivity, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
