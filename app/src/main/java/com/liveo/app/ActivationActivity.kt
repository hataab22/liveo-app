package com.liveo.app

import retrofit2.http.GET
import retrofit2.http.Query

data class ActivationCode(
    val code: String,
    val isValid: Boolean
)

data class ActivationResponse(
    val isValid: Boolean,
    val activationCode: ActivationCode
)

interface ActivationApi {
    @GET("liveo-codes.json")
    suspend fun validateCode(@Query("code") code: String): ActivationResponse
}
