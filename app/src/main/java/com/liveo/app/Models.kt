package com.liveo.app

import retrofit2.http.GET
import retrofit2.http.Query
import java.util.UUID

data class Channel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val logo: String = "",
    val category: String = ""
)

// Activation models
data class ActivationCode(
    val code: String,
    val isValid: Boolean
)

data class ActivationResponse(
    val isValid: Boolean,
    val activationCode: ActivationCode
)

// PIN Verification
data class PinVerificationResponse(
    val success: Boolean = false,
    val valid: Boolean = false,
    val message: String = "",
    val isValid: Boolean = false
)

// Activation API Interface
interface ActivationApi {
    @GET("liveo-codes.json")
    suspend fun validateCode(@Query("code") code: String): ActivationResponse
}
