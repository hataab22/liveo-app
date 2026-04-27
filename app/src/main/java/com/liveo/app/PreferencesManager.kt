package com.liveo.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "LiveoPrefs"
        private const val KEY_ACTIVATED = "is_activated"
        private const val KEY_ACTIVATION_CODE = "activation_code"
        private const val KEY_PARENTAL_UNLOCKED = "parental_unlocked"
        private const val KEY_ADULT_ACCESS = "adult_access"
        private const val KEY_PARENTAL_PIN = "parental_pin"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_RECENT = "recent"
    }
    
    fun saveActivation(code: String, adultAccess: Boolean = false, parentalPin: String = "") {
        prefs.edit().apply {
            putBoolean(KEY_ACTIVATED, true)
            putString(KEY_ACTIVATION_CODE, code)
            putBoolean(KEY_ADULT_ACCESS, adultAccess)
            putString(KEY_PARENTAL_PIN, parentalPin)
            apply()
        }
    }
    
    fun isActivated(): Boolean = prefs.getBoolean(KEY_ACTIVATED, false)
    
    fun hasAdultAccess(): Boolean = prefs.getBoolean(KEY_ADULT_ACCESS, false)
    
    fun getParentalPin(): String? = prefs.getString(KEY_PARENTAL_PIN, null)
    
    fun clearActivation() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }
    
    fun setParentalUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_PARENTAL_UNLOCKED, unlocked).apply()
    }
    
    fun isParentalUnlocked(): Boolean = prefs.getBoolean(KEY_PARENTAL_UNLOCKED, false)
