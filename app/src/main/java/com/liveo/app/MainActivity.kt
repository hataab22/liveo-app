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
        Toast.makeText(this, "جاري التحميل...", Toast.LENGTH_SHORT).show()
        
        val code = prefsManager.getActivationCode()?.code
        val m3uUrl = "https://liveo-backend.onrender.com/api/playlist/$code"
        
        Log.d(TAG, "Loading from URL: $m3uUrl")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val channels = withTimeout(45000L) {
                    withContext(Dispatchers.IO) {
                        try {
                            M3UParser.parseFromUrl(m3uUrl)
                        } catch (e: Exception) {
                            Log.e(TAG, "Parser error", e)
                            emptyList()
                        }
                    }
                }
                
                Log.d(TAG, "Channels received: ${channels.size}")
                
                // ✅ Fallback: لو ما لقى قنوات، نستخدم قنوات تجريبية
                val finalChannels = if (channels.isEmpty()) {
                    Log.w(TAG, "No channels from API, using fallback")
                    Toast.makeText(this@MainActivity, "استخدام قنوات تجريبية...", Toast.LENGTH_SHORT).show()
                    getFallbackChannels()
                } else {
                    channels
                }
                
                if (finalChannels.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "تم تحميل ${finalChannels.size} قناة", Toast.LENGTH_SHORT).show()
                    
                    val adapter = ViewPagerAdapter(supportFragmentManager, finalChannels, prefsManager)
                    viewPager.adapter = adapter
                    tabLayout.setupWithViewPager(viewPager)
                } else {
                    Toast.makeText(this@MainActivity, "لم يتم العثور على قنوات", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading channels", e)
                Toast.makeText(this@MainActivity, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                
                // ✅ استخدم قنوات تجريبية عند الخطأ
                loadFallbackChannels()
            }
        }
    }
    
    private fun loadFallbackChannels() {
        val channels = getFallbackChannels()
        if (channels.isNotEmpty()) {
            Toast.makeText(this, "تم تحميل ${channels.size} قناة تجريبية", Toast.LENGTH_SHORT).show()
            val adapter = ViewPagerAdapter(supportFragmentManager, channels, prefsManager)
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)
        }
    }
    
    private fun getFallbackChannels(): List<Channel> {
        return listOf(
            Channel(
                "1", 
                "Big Buck Bunny", 
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "", 
                "عام"
            ),
            Channel(
                "2", 
                "Elephant Dream", 
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "", 
                "عام"
            ),
            Channel(
                "3", 
                "For Bigger Blazes", 
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "", 
                "عام"
            ),
            Channel(
                "4", 
                "Sintel", 
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                "", 
                "عام"
            ),
            Channel(
                "5", 
                "Tears of Steel", 
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                "", 
                "عام"
            )
        )
    }
}
