package com.liveo.app

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    
    // Backend URL
    private const val BASE_URL = "https://liveo-backend.onrender.com"
    
    private val gson = Gson()
    
    data class ActivateRequest(val code: String, val device_id: String)
    data class ActivateResponse(
        val success: Boolean,
        val code: String? = null,
        val m3u_url: String? = null,
        val expires_at: Long? = null,
        val customer_name: String? = null,
        val days_left: Int? = null,
        val message: String? = null,
        val parental_pin: String? = null
    )
    
    suspend fun activateCode(code: String, deviceId: String): ActivateResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/activate")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val request = ActivateRequest(code.uppercase(), deviceId)
            val jsonInput = gson.toJson(request)
            
            connection.outputStream.use { it.write(jsonInput.toByteArray()) }
            
            val response = connection.inputStream.bufferedReader().readText()
            gson.fromJson(response, ActivateResponse::class.java)
            
        } catch (e: Exception) {
            e.printStackTrace()
            ActivateResponse(
                success = false,
                message = "خطأ في الاتصال: ${e.message}"
            )
        }
    }
    
    data class VerifyPinRequest(val code: String, val pin: String)
    
    suspend fun verifyPin(code: String, pin: String): PinVerificationResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/verify_pin")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val request = VerifyPinRequest(code.uppercase(), pin)
            val jsonInput = gson.toJson(request)
            
            connection.outputStream.use { it.write(jsonInput.toByteArray()) }
            
            val response = connection.inputStream.bufferedReader().readText()
            gson.fromJson(response, PinVerificationResponse::class.java)
            
        } catch (e: Exception) {
            e.printStackTrace()
            PinVerificationResponse(
                success = false,
                valid = false,
                message = "خطأ في الاتصال: ${e.message}"
            )
        }
    }
}
