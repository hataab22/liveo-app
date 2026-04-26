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
            Channel("1", "الجزيرة", "https://live-hls-web-aja.getaj.net/AJA/01.m3u8", "https://i.imgur.com/x9TQYI3.png", "أخبار"),
            Channel("2", "العربية", "https://live.alarabiya.net/alarabiapublish/alarabiya.smil/playlist.m3u8", "https://i.imgur.com/7bRVpnu.png", "أخبار"),
            Channel("3", "دبي", "https://dmisxthvll.cdn.mgmlcdn.com/dubaitvht/smil:dubaitv.stream.smil/playlist.m3u8", "https://i.imgur.com/ST7OiMl.png", "عام"),
            Channel("4", "MBC 1", "https://d3o3cim6uzorb4.cloudfront.net/out/v1/0965e4d7deae49179172426cbfb3bc5e/index.m3u8", "https://i.imgur.com/V3WM43w.png", "عام"),
            Channel("5", "دبي الرياضية", "https://dmitnthvll.cdn.mgmlcdn.com/dubaisports/smil:dubaisports.smil/playlist.m3u8", "https://i.imgur.com/qL9kYdW.png", "رياضة")
        )
    }
}
