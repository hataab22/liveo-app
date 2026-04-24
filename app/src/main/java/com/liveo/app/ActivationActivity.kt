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
import kotlinx.coroutines.delay

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
            try {
                val response = ApiClient.activateCode(code, deviceId)
                
                showLoading(false)
                
                if (response.success && response.m3u_url != null) {
                    // حفظ معلومات الكود
                    val activationCode = ActivationCode(
                        code = response.code ?: code,
                        expiryDate = response.expires_at ?: 0,
                        isActive = true,
                        customerName = response.customer_name ?: ""
                    )
                    prefsManager.saveActivationCode(activationCode)
                    prefsManager.saveM3uUrl(response.m3u_url)
                    
                    // جلب القنوات في background thread
                    val channels = withContext(Dispatchers.IO) {
                        M3UParser.parseFromUrl(response.m3u_url)
                    }
                    
                    // العودة للـ Main thread
                    withContext(Dispatchers.Main) {
                        if (channels.isNotEmpty()) {
                            Toast.makeText(
                                this@ActivationActivity, 
                                "تم التفعيل! ${channels.size} قناة", 
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // انتظار قصير لضمان حفظ البيانات
                            delay(500)
                            
                            // الانتقال للصفحة الرئيسية
                            navigateToMain()
                        } else {
                            showError("لم يتم العثور على قنوات")
                        }
                    }
                } else {
                    showError(response.message ?: "خطأ في التفعيل")
                }
            } catch (e: Exception) {
                showLoading(false)
                showError("حدث خطأ: ${e.message}")
            }
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
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("FROM_ACTIVATION", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
