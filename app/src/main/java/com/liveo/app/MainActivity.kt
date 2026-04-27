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
    
    private lateinit var prefsManager: PreferencesManager
    private var allChannels = listOf<Channel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefsManager = PreferencesManager(this)
        
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        loadChannels()
    }
    
    private fun loadChannels() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://drive.google.com/uc?export=download&id=1EED9-uQPohWSo2mPYtT9Ji9hr7wBzg4A"
                val parser = M3UParser(url)
                allChannels = parser.parse()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "تم تحميل ${allChannels.size} قناة", Toast.LENGTH_SHORT).show()
                    setupViewPager()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                    setupViewPager()
                }
            }
        }
    }
    
    private fun setupViewPager() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        
        val adapter = ViewPagerAdapter(supportFragmentManager, allChannels, prefsManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        
        val lockItem = menu.findItem(R.id.action_parental_lock)
        lockItem?.title = if (prefsManager.isParentalUnlocked()) {
            "تفعيل الرقابة الأبوية"
        } else {
            "تعطيل الرقابة الأبوية"
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
            prefsManager.setParentalUnlocked(false)
            Toast.makeText(this, "تم تفعيل الرقابة الأبوية", Toast.LENGTH_SHORT).show()
            invalidateOptionsMenu()
            recreate()
        } else {
            showPinDialog()
        }
    }
    
    private fun showPinDialog() {
        val savedPin = prefsManager.getParentalPin()
        
        if (savedPin.isNullOrEmpty()) {
            Toast.makeText(this, "الحساب غير مصرح له بالمحتوى الكبار", Toast.LENGTH_LONG).show()
            return
        }
        
        val input = EditText(this).apply {
            hint = "أدخل رمز PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        
        AlertDialog.Builder(this)
            .setTitle("الرقابة الأبوية")
            .setMessage("أدخل رمز PIN للمحتوى الكبار")
            .setView(input)
            .setPositiveButton("تأكيد") { _, _ ->
                if (input.text.toString() == savedPin) {
                    prefsManager.setParentalUnlocked(true)
                    Toast.makeText(this, "تم تعطيل الرقابة الأبوية", Toast.LENGTH_SHORT).show()
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
            .setMessage("هل تريد تسجيل الخروج؟")
            .setPositiveButton("نعم") { _, _ ->
                prefsManager.clearActivation()
                startActivity(Intent(this, ActivationActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("لا", null)
            .show()
    }
}
