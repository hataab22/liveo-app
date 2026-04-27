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

class MainActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            
            prefsManager = PreferencesManager(this)
            
            // Setup toolbar
            try {
                val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                setSupportActionBar(toolbar)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Setup basic UI
            val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
            val viewPager = findViewById<ViewPager>(R.id.viewPager)
            
            // Simple adapter with empty channels for now
            val adapter = ViewPagerAdapter(supportFragmentManager, emptyList(), prefsManager)
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)
            
            Toast.makeText(this, "مرحباً في Liveo!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        try {
            menuInflater.inflate(R.menu.menu_main, menu)
            val lockItem = menu.findItem(R.id.action_parental_lock)
            if (lockItem != null) {
                lockItem.title = if (prefsManager.isParentalUnlocked()) {
                    "تفعيل الرقابة الأبوية"
                } else {
                    "تعطيل الرقابة الأبوية"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        } else {
            showPinDialog()
        }
    }
    
    private fun showPinDialog() {
        val input = EditText(this).apply {
            hint = "أدخل رمز PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        
        AlertDialog.Builder(this)
            .setTitle("الرقابة الأبوية")
            .setMessage("أدخل رمز PIN (1234)")
            .setView(input)
            .setPositiveButton("تأكيد") { _, _ ->
                if (input.text.toString() == "1234") {
                    prefsManager.setParentalUnlocked(true)
                    Toast.makeText(this, "تم تعطيل الرقابة الأبوية", Toast.LENGTH_SHORT).show()
                    invalidateOptionsMenu()
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
