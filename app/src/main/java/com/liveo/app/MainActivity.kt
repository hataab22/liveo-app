package com.liveo.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var prefsManager: PreferencesManager
    private var allChannels = listOf<Channel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefsManager = PreferencesManager(this)
        
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        
        loadChannels()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        
        // Update parental lock menu item title
        val lockItem = menu.findItem(R.id.action_parental_lock)
        lockItem.title = if (prefsManager.isParentalUnlocked()) {
            "تفعيل القفل الأبوي"
        } else {
            "تعطيل القفل الأبوي"
        }
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_parental_lock -> {
                toggleParentalLock()
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun toggleParentalLock() {
        if (prefsManager.isParentalUnlocked()) {
            // Lock it
            prefsManager.setParentalUnlocked(false)
            Toast.makeText(this, "تم تفعيل القفل الأبوي", Toast.LENGTH_SHORT).show()
            invalidateOptionsMenu() // Refresh menu
            recreate() // Reload fragments
        } else {
            // Ask for PIN to unlock
            showPinDialog()
        }
    }
    
    private fun showPinDialog() {
        val input = EditText(this).apply {
            hint = "أدخل رمز PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or 
                        android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        
        AlertDialog.Builder(this)
            .setTitle("القفل الأبوي")
            .setMessage("أدخل رمز PIN (افتراضي: 1234)")
            .setView(input)
            .setPositiveButton("تأكيد") { _, _ ->
                val pin = input.text.toString()
                if (pin == "1234") {
                    prefsManager.setParentalUnlocked(true)
                    Toast.makeText(this, "تم تعطيل القفل الأبوي", Toast.LENGTH_SHORT).show()
                    invalidateOptionsMenu()
                    recreate()
                } else {
                    Toast.makeText(this, "رمز PIN خاطئ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("تسجيل الخروج")
            .setMessage("هل تريد تسجيل الخروج من التطبيق؟")
            .setPositiveButton("نعم") { _, _ ->
                prefsManager.clearActivation()
                val intent = Intent(this, ActivationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("لا", null)
            .show()
    }
    
    private fun loadChannels() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://drive.google.com/uc?export=download&id=1EED9-uQPohWSo2mPYtT9Ji9hr7wBzg4A"
                val parser = M3UParser(url)
                allChannels = parser.parse(url)
                
                withContext(Dispatchers.Main) {
                    setupViewPager()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "خطأ في تحميل القنوات", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(supportFragmentManager, allChannels, prefsManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}
