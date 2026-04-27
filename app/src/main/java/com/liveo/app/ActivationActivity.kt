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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ActivationActivity : AppCompatActivity() {
    
    private lateinit var codeInput: EditText
    private lateinit var activateButton: Button
    private lateinit var prefsManager: PreferencesManager
    
    private val BASE_URL = "https://liveo-backend.onrender.com"
    
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
            val endpoints = listOf(
                "/api/validate?code=$code",
                "/validate?code=$code",
                "/api/codes/validate?code=$code",
                "/check?code=$code",
                "/api/check?code=$code",
                "/codes/validate?code=$code"
            )
            
            var success = false
            var adultAccess = false
            
            for (endpoint in endpoints) {
                try {
                    val url = URL("$BASE_URL$endpoint")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    
                    val responseCode = connection.responseCode
                    
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().readText()
                        Log.d("ActivationActivity", "Endpoint: $endpoint - Response: $response")
                        
                        try {
                            val json = JSONObject(response)
                            val isValid = json.optBoolean("valid", false) || 
                                         json.optBoolean("isValid", false) ||
                                         json.optBoolean("success", false) ||
                                         json.optBoolean("active", false)
                            
                            if (isValid) {
                                adultAccess = json.optBoolean("adult_access", false) ||
                                            json.optBoolean("adultAccess", false)
                                success = true
                                Log.d("ActivationActivity", "SUCCESS! Endpoint: $endpoint")
                                break
                            }
                        } catch (e: Exception) {
                            Log.e("ActivationActivity", "JSON parse error: ${e.message}")
                        }
                    }
                    
                    connection.disconnect()
                    
                } catch (e: Exception) {
                    Log.e("ActivationActivity", "Endpoint $endpoint failed: ${e.message}")
                }
            }
            
            withContext(Dispatchers.Main) {
                if (success) {
                    prefsManager.saveActivation(code, adultAccess)
                    Toast.makeText(this@ActivationActivity, "تم التفعيل بنجاح!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(this@ActivationActivity, "كود التفعيل غير صحيح أو خطأ في الاتصال", Toast.LENGTH_LONG).show()
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
