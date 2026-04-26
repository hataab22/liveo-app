package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivationActivity : AppCompatActivity() {
    
    private lateinit var codeInput: EditText
    private lateinit var activateButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)
        
        prefsManager = PreferencesManager(this)
        
        codeInput = findViewById(R.id.codeInput)
        activateButton = findViewById(R.id.activateButton)
        progressBar = findViewById(R.id.progressBar)
        
        activateButton.setOnClickListener {
            val code = codeInput.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "الرجاء إدخال كود التفعيل", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            activateCode(code)
        }
    }
    
    private fun activateCode(code: String) {
        progressBar.isVisible = true
        activateButton.isEnabled = false
        
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        
        CoroutineScope(Dispatchers.Main).launch {
            val response = ApiClient.activateCode(code, deviceId)
            
            progressBar.isVisible = false
            activateButton.isEnabled = true
            
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
    
    private suspend fun loadChannels(m3uUrl: String) {
        try {
            val channels = withContext(Dispatchers.IO) {
                M3UParser.parseFromUrl(m3uUrl)
            }
            
            if (channels.isNotEmpty()) {
                Toast.makeText(this, "تم التفعيل بنجاح", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                showError("لم يتم العثور على قنوات")
            }
        } catch (e: Exception) {
            showError("خطأ في تحميل القنوات: ${e.message}")
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
