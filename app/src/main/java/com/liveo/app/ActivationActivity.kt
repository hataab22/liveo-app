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
            if (code.isNotEmpty()) {
                activateCode(code)
            } else {
                Toast.makeText(this, "أدخل الكود", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun activateCode(code: String) {
        activateButton.isEnabled = false
        activateButton.text = "جاري التفعيل..."
        
        val deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.activateCode(code, deviceId)
                
                CoroutineScope(Dispatchers.Main).launch {
                    if (response.success) {
                        val activationCode = ActivationCode(
                            code = response.code ?: code,
                            expiryDate = response.expires_at ?: 0,
                            isActive = true,
                            customerName = response.customer_name ?: "",
                            parentalPin = response.parental_pin
                        )
                        prefsManager.saveActivationCode(response.activationCode.code)
                        
                        Toast.makeText(this@ActivationActivity, "تم التفعيل بنجاح ✓", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@ActivationActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        activateButton.isEnabled = true
                        activateButton.text = "تفعيل"
                        Toast.makeText(this@ActivationActivity, response.message ?: "فشل التفعيل", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    activateButton.isEnabled = true
                    activateButton.text = "تفعيل"
                    Toast.makeText(this@ActivationActivity, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
