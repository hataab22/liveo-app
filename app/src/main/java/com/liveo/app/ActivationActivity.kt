package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ActivationActivity : AppCompatActivity() {
    
    private lateinit var codeInput: EditText
    private lateinit var activateButton: Button
    private lateinit var prefsManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)
        
        prefsManager = PreferencesManager(this)
        
        // تحقق إذا المستخدم مفعّل
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
            
            // قائمة الأكواد الصحيحة
            val validCodes = listOf(
                "123456",
                "LIVEO2024",
                "IPTV2024",
                "TEST123"
            )
            
            if (validCodes.contains(code.uppercase())) {
                // الكود صحيح
                prefsManager.saveActivation(code)
                Toast.makeText(this, "تم التفعيل بنجاح!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                // الكود خاطئ
                Toast.makeText(this, "كود التفعيل غير صحيح", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
