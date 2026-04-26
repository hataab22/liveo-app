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
            Log.d(TAG, "Activation code: ${activation?.code}")
            
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
        Toast.makeText(this, "جاري تحميل قنواتك...", Toast.LENGTH_SHORT).show()
        
        // ✅ رابط Google Drive الخاص بك
        val m3uUrl = "https://drive.google.com/uc?export=download&id=1EED9-uQPohWSo2mPYtT9Ji9hr7wBzg4A"
        
        Log.d(TAG, "Loading from: $m3uUrl")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val channels = withTimeout(60000L) { // 60 seconds
                    withContext(Dispatchers.IO) {
                        try {
                            M3UParser.parseFromUrl(m3uUrl)
                        } catch (e: Exception) {
                            Log.e(TAG, "Parser error", e)
                            emptyList()
                        }
                    }
                }
                
                Log.d(TAG, "Channels loaded: ${channels.size}")
                
                if (channels.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "تم تحميل ${channels.size} قناة بنجاح!", Toast.LENGTH_SHORT).show()
                    
                    val adapter = ViewPagerAdapter(supportFragmentManager, channels, prefsManager)
                    viewPager.adapter = adapter
                    tabLayout.setupWithViewPager(viewPager)
                } else {
                    Toast.makeText(this@MainActivity, "لم يتم العثور على قنوات في الملف", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "Timeout loading channels", e)
                Toast.makeText(this@MainActivity, "انتهت مهلة التحميل. تحقق من الاتصال", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading channels", e)
                Toast.makeText(this@MainActivity, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
