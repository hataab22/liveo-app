package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ActivationActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            
            if (code.isNotEmpty()) {
                prefsManager.saveActivation(code, true, "1234")
                Toast.makeText(this, "تم التفعيل بنجاح!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                Toast.makeText(this, "الرجاء إدخال كود التفعيل", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
