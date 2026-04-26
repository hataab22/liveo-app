package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ActivationActivity : AppCompatActivity() {
    
    private lateinit var codeInput: EditText
    private lateinit var activateButton: Button
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)
        
        supportActionBar?.hide()
        
        prefsManager = PreferencesManager(this)
        
        setupViews()
        checkExistingCode()
    }
    
    private fun setupViews() {
        codeInput = findViewById(R.id.codeInput)
        activateButton = findViewById(R.id.activateButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        errorText = findViewById(R.id.errorText)
        
        activateButton.setOnClickListener {
            activateCode()
        }
    }
    
    private fun checkExistingCode() {
        if (prefsManager.isCodeValid()) {
            navigateToMain()
        }
    }
    
    private fun activateCode() {
        val code = codeInput.text.toString().trim()
        
        if (code.isEmpty()) {
            showError("الرجاء إدخال كود التفعيل")
            return
        }
        
        showLoading(true)
        errorText.visibility = View.GONE
        
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        
        CoroutineScope(Dispatchers.Main).launch {
            val response = ApiClient.activateCode(code, deviceId)
            
            withContext(Dispatchers.Main) {
                showLoading(false)
                
                if (response.success && response.m3u_url != null) {
                    // حفظ معلومات الكود
                    val activationCode = ActivationCode(
                        code = response.code ?: code,
                        expiryDate = response.expires_at ?: 0,
                        isActive = true,
                        customerName = response.customer_name ?: "",
                        parentalPin = response.parental_pin
                    )
                    prefsManager.saveActivationCode(activationCode)
                    
                    // جلب القنوات
                    loadChannels(response.m3u_url)
                } else {
                    showError(response.message ?: "خطأ في التفعيل")
                }
            }
        }
    }
    
    private suspend fun loadChannels(m3uUrl: String) {
        val channels = M3UParser.parseFromUrl(m3uUrl)
        
        if (channels.isNotEmpty()) {
            Toast.makeText(this, "تم التفعيل! ${channels.size} قناة", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            showError("لم يتم العثور على قنوات")
        }
    }
    
    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
    }
    
    private fun showLoading(show: Boolean) {
        loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        activateButton.isEnabled = !show
        codeInput.isEnabled = !show
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
