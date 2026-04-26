package com.liveo.app

import android.content.Context
import com.google.gson.Gson
import java.io.IOException

object ConfigManager {
    
    fun loadConfig(context: Context): AppConfig {
        return try {
            val json = context.assets.open("config.json")
                .bufferedReader()
                .use { it.readText() }
            Gson().fromJson(json, AppConfig::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            AppConfig(version = "2.0", categories = emptyList())
        }
    }
}
