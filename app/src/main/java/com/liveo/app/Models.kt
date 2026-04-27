package com.liveo.app

import retrofit2.http.GET
import retrofit2.http.Query

data class Channel(
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

// Activation API Interface
interface ActivationApi {
    @GET("liveo-codes.json")
    suspend fun validateCode(@Query("code") code: String): ActivationResponse
}
