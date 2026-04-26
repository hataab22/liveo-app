package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivationActivity : AppCompatActivity() {
    
    private lateinit var codeInput: EditText
    private lateinit var activateButton: Button
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)
        
        prefsManager = PreferencesManager(this)
        
        codeInput = findViewById(R.id.codeInput)
        activateButton = findViewById(R.id.activateButton)
        
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
        activateButton.isEnabled = false
        
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = ApiClient.activateCode(code, deviceId)
                
                activateButton.isEnabled = true
                
                if (response.success) {
                    // حفظ بيانات التفعيل
                    val activationCode = ActivationCode(
                        code = response.code ?: code,
                        expiryDate = response.expires_at ?: 0,
                        isActive = true,
                        customerName = response.customer_name ?: "",
                        parentalPin = response.parental_pin
                    )
                    prefsManager.saveActivationCode(activationCode)
                    
                    Toast.makeText(this@ActivationActivity, "تم التفعيل بنجاح", Toast.LENGTH_SHORT).show()
                    
                    // تحميل القنوات في الخلفية (اختياري)
                    if (response.m3u_url != null && response.m3u_url.isNotEmpty()) {
                        loadChannelsInBackground(response.m3u_url)
                    }
                    
                    // الانتقال للصفحة الرئيسية فوراً (بدون انتظار القنوات)
                    navigateToMain()
                } else {
                    showError(response.message ?: "خطأ في التفعيل")
                }
            } catch (e: Exception) {
                activateButton.isEnabled = true
                showError("خطأ في الاتصال: ${e.message}")
            }
        }
    }
    
    private fun loadChannelsInBackground(m3uUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val channels = M3UParser.parseFromUrl(m3uUrl)
                if (channels.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ActivationActivity,
                            "تم تحميل ${channels.size} قناة",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // تجاهل الأخطاء - القنوات ستُحمّل من MainActivity
            }
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
