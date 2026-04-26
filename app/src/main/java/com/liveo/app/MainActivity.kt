package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
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
    private var progressBar: ProgressBar? = null
    
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
        progressBar = findViewById(R.id.progressBar)
        
        loadChannels()
    }
    
    private fun loadChannels() {
        showLoading(true)
        
        val m3uUrl = "https://liveo-backend.onrender.com/api/playlist/${prefsManager.getActivationCode()?.code}"
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Toast.makeText(this@MainActivity, "جاري تحميل القنوات...", Toast.LENGTH_SHORT).show()
                
                val channels = withTimeout(30000L) { // 30 seconds timeout
                    withContext(Dispatchers.IO) {
                        M3UParser.parseFromUrl(m3uUrl)
                    }
                }
                
                showLoading(false)
                
                if (channels.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "تم تحميل ${channels.size} قناة", Toast.LENGTH_SHORT).show()
                    val adapter = ViewPagerAdapter(supportFragmentManager, channels, prefsManager)
                    viewPager.adapter = adapter
                    tabLayout.setupWithViewPager(viewPager)
                } else {
                    Toast.makeText(this@MainActivity, "لا توجد قنوات متاحة", Toast.LENGTH_LONG).show()
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                showLoading(false)
                Toast.makeText(this@MainActivity, "انتهت مهلة التحميل. الرجاء المحاولة مرة أخرى", Toast.LENGTH_LONG).show()
            } catch (e: OutOfMemoryError) {
                showLoading(false)
                Toast.makeText(this@MainActivity, "الملف كبير جداً. جاري إعادة المحاولة...", Toast.LENGTH_LONG).show()
                System.gc() // تنظيف الذاكرة
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@MainActivity, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
        viewPager.visibility = if (show) View.GONE else View.VISIBLE
        tabLayout.visibility = if (show) View.GONE else View.VISIBLE
    }
}
