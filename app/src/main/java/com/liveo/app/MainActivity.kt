package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    
    private lateinit var prefsManager: PreferencesManager
    private var allChannels = listOf<Channel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PreferencesManager(this)
        
        // إعادة تعيين قفل الرقابة الأبوية
        prefsManager.resetParentalLock()
        
        // التحقق من التفعيل
        val activation = prefsManager.getActivationCode()
        if (activation == null || !activation.isActive) {
            navigateToActivation()
            return
        }
        
        setContentView(R.layout.activity_main)
        
        setupUI()
        loadChannels()
    }
    
    private fun setupUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        searchView = findViewById(R.id.searchView)
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            
            override fun onQueryTextChange(newText: String?): Boolean {
                filterChannels(newText ?: "")
                return true
            }
        })
    }
    
    private fun loadChannels() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val activation = prefsManager.getActivationCode()
                if (activation == null) {
                    navigateToActivation()
                    return@launch
                }
                
                val response = withContext(Dispatchers.IO) {
                    ApiClient.verifyActivation(activation.code)
                }
                
                if (response.success && response.m3u_url != null) {
                    allChannels = withContext(Dispatchers.IO) {
                        M3UParser.parseFromUrl(response.m3u_url)
                    }
                    
                    if (allChannels.isNotEmpty()) {
                        setupViewPager(allChannels)
                    } else {
                        Toast.makeText(this@MainActivity, "لا توجد قنوات متاحة", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "انتهت صلاحية التفعيل", Toast.LENGTH_LONG).show()
                    prefsManager.clearActivationCode()
                    navigateToActivation()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطأ في تحميل القنوات", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupViewPager(channels: List<Channel>) {
        val adapter = ViewPagerAdapter(supportFragmentManager, channels, prefsManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
    
    private fun filterChannels(query: String) {
        val currentFragment = (viewPager.adapter as? ViewPagerAdapter)
            ?.getItem(viewPager.currentItem)
        
        when (currentFragment) {
            is AllChannelsFragment -> currentFragment.search(query)
            is FavoritesFragment -> currentFragment.search(query)
            is RecentFragment -> currentFragment.search(query)
        }
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_parental_controls -> {
                val intent = Intent(this, ParentalControlActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "الإعدادات قريباً", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_about -> {
                Toast.makeText(this, "Liveo IPTV v2.0", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun navigateToActivation() {
        val intent = Intent(this, ActivationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
