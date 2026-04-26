package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    
    private val TAG = "ActivationActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PreferencesManager(this)
        
        // التحقق إذا كان التفعيل موجود بالفعل
        val existingActivation = prefsManager.getActivationCode()
        if (existingActivation != null && existingActivation.isActive) {
            Log.d(TAG, "التفعيل موجود بالفعل، الانتقال للصفحة الرئيسية")
            navigateToMain()
            return
        }
        
        setContentView(R.layout.activity_activation)
        
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
        Log.d(TAG, "بدء التفعيل للكود: $code")
        
        activateButton.isEnabled = false
        activateButton.text = "جاري التفعيل..."
        
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "إرسال طلب التفعيل للسيرفر...")
                
                val response = withContext(Dispatchers.IO) {
                    ApiClient.activateCode(code, deviceId)
                }
                
                Log.d(TAG, "استجابة السيرفر: success=${response.success}, message=${response.message}")
                
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
                    Log.d(TAG, "تم حفظ بيانات التفعيل بنجاح")
                    
                    Toast.makeText(this@ActivationActivity, "تم التفعيل بنجاح ✓", Toast.LENGTH_SHORT).show()
                    
                    // الانتقال للصفحة الرئيسية
                    Log.d(TAG, "الانتقال للصفحة الرئيسية...")
                    navigateToMain()
                } else {
                    activateButton.isEnabled = true
                    activateButton.text = "تفعيل"
                    showError(response.message ?: "خطأ في التفعيل")
                }
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في التفعيل", e)
                activateButton.isEnabled = true
                activateButton.text = "تفعيل"
                showError("خطأ في الاتصال: ${e.message}")
            }
        }
    }
    
    private fun showError(message: String) {
        Log.e(TAG, "خطأ: $message")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun navigateToMain() {
        Log.d(TAG, "navigateToMain() called")
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        Log.d(TAG, "Starting MainActivity...")
        startActivity(intent)
        
        Log.d(TAG, "Finishing ActivationActivity...")
        finish()
    }
}
