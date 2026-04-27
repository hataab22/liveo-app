package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ActivationActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_activation)
            
            prefsManager = PreferencesManager(this)
            
            if (prefsManager.isActivated()) {
                navigateToMain()
                return
            }
            
            val codeInput = findViewById<EditText>(R.id.codeInput)
            val activateButton = findViewById<Button>(R.id.activateButton)
            
            activateButton.setOnClickListener {
                val code = codeInput.text.toString().trim()
                
                if (code.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال كود التفعيل", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                activateButton.isEnabled = false
                activateButton.text = "جاري التحقق..."
                
                validateCode(code, activateButton)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun validateCode(code: String, button: Button) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                
                val url = URL("https://liveo-backend.onrender.com/activate")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                val jsonInput = JSONObject().apply {
                    put("code", code.uppercase())
                    put("device_id", deviceId)
                }
                
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonInput.toString())
                writer.flush()
                writer.close()
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)
                    
                    val success = json.optBoolean("success", false)
                    
                    withContext(Dispatchers.Main) {
                        if (success) {
                            val parentalPin = json.optString("parental_pin", "")
                            val hasAdultAccess = parentalPin.isNotEmpty()
                            
                            prefsManager.saveActivation(code, hasAdultAccess, parentalPin)
                            Toast.makeText(this@ActivationActivity, "تم التفعيل بنجاح!", Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        } else {
                            val message = json.optString("message", "كود التفعيل غير صحيح")
                            Toast.makeText(this@ActivationActivity, message, Toast.LENGTH_LONG).show()
                            button.isEnabled = true
                            button.text = "تفعيل"
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ActivationActivity, "خطأ في الاتصال بالخادم", Toast.LENGTH_SHORT).show()
                        button.isEnabled = true
                        button.text = "تفعيل"
                    }
                }
                
                connection.disconnect()
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ActivationActivity, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                    button.isEnabled = true
                    button.text = "تفعيل"
                }
            }
        }
    }
    
    private fun navigateToMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "خطأ في الانتقال: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
