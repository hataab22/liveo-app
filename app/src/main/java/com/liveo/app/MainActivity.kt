package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var prefsManager: PreferencesManager
    private lateinit var expiryText: TextView
    
    private var allChannels = listOf<Channel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefsManager = PreferencesManager(this)
        
        val fromActivation = intent.getBooleanExtra("FROM_ACTIVATION", false)
        
        if (!fromActivation && !prefsManager.isCodeValid()) {
            navigateToActivation()
            return
        }
        
        setupViews()
        loadChannels()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                loadChannels()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        searchView = findViewById(R.id.searchView)
        expiryText = findViewById(R.id.expiryText)
        
        updateExpiryInfo()
        setupSearch()
    }
    
    private fun updateExpiryInfo() {
        val code = prefsManager.getActivationCode()
        if (code != null) {
            if (code.expiryDate == 0L) {
                expiryText.text = "غير محدود"
            } else {
                val daysLeft = ((code.expiryDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
                expiryText.text = "متبقي: $daysLeft يوم"
                
                if (daysLeft <= 3) {
                    expiryText.setTextColor(getColor(R.color.warning))
                }
            }
        }
    }
    
    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            
            override fun onQueryTextChange(newText: String?): Boolean {
                // سيتم تنفيذها في Fragments
                return true
            }
        })
    }
    
    private fun loadChannels() {
        val m3uUrl = prefsManager.getM3uUrl()
        
        if (m3uUrl == null) {
            Toast.makeText(this, "خطأ: لم يتم العثور على رابط القنوات", Toast.LENGTH_LONG).show()
            navigateToActivation()
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                allChannels = withContext(Dispatchers.IO) {
                    M3UParser.parseFromUrl(m3uUrl)
                }
                
                if (allChannels.isEmpty()) {
                    Toast.makeText(this@MainActivity, "لم يتم العثور على قنوات", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                // تحديث حالة المفضلة
                val favorites = prefsManager.getFavorites()
                allChannels.forEach { channel ->
                    channel.isFavorite = favorites.any { it.id == channel.id }
                }
                
                setupViewPager()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطأ في تحميل القنوات: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this, allChannels, prefsManager)
        viewPager.adapter = adapter
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "الكل"
                1 -> "المفضلة"
                2 -> "الأخيرة"
                else -> "الكل"
            }
        }.attach()
    }
    
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("تسجيل خروج")
            .setMessage("هل تريد تسجيل الخروج؟")
            .setPositiveButton("نعم") { _, _ ->
                prefsManager.clearActivationCode()
                navigateToActivation()
            }
            .setNegativeButton("لا", null)
            .show()
    }
    
    private fun navigateToActivation() {
        val intent = Intent(this, ActivationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
