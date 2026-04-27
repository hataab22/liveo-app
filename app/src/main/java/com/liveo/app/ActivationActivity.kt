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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ActivationActivity : AppCompatActivity() {
    
    private lateinit var codeInput: EditText
    private lateinit var activateButton: Button
    private lateinit var prefsManager: PreferencesManager
    
    private val API_URL = "https://liveo-backend.onrender.com"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)
        
        prefsManager = PreferencesManager(this)
        
        if (prefsManager.isActivated()) {
            navigateToMain()
            return
        }
        
        codeInput = findViewById(R.id.codeInput)
        activateButton = findViewById(R.id.activateButton)
        
        activateButton.setOnClickListener {
            val code = codeInput.text.toString().trim()
            
            if (code.isEmpty()) {
                Toast.makeText(this, "الرجاء إدخال كود التفعيل", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            activateButton.isEnabled = false
            activateButton.text = "جاري التحقق..."
            
            validateCode(code)
        }
    }
    
    private fun validateCode(code: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // جرب endpoint: /api/validate
                val url = URL("$API_URL/api/validate?code=$code")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonObject = JSONObject(response)
                    
                    val isValid = jsonObject.optBoolean("valid", false) || 
                                 jsonObject.optBoolean("isValid", false) ||
                                 jsonObject.optBoolean("success", false)
                    
                    val hasAdultAccess = jsonObject.optBoolean("adult_access", false) ||
                                        jsonObject.optBoolean("adultAccess", false)
                    
                    withContext(Dispatchers.Main) {
                        if (isValid) {
                            prefsManager.saveActivation(code, hasAdultAccess)
                            Toast.makeText(this@ActivationActivity, "تم التفعيل بنجاح!", Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        } else {
                            Toast.makeText(this@ActivationActivity, "كود التفعيل غير صحيح", Toast.LENGTH_SHORT).show()
                            resetButton()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ActivationActivity, "خطأ في الاتصال بالخادم", Toast.LENGTH_SHORT).show()
                        resetButton()
                    }
                }
                
                connection.disconnect()
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ActivationActivity, 
                        "خطأ في الاتصال: ${e.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                    resetButton()
                }
            }
        }
    }
    
    private fun resetButton() {
        activateButton.isEnabled = true
        activateButton.text = "تفعيل"
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
