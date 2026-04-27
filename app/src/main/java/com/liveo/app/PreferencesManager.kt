package com.liveo.app

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "LiveoPrefs"
        private const val KEY_ACTIVATED = "is_activated"
        private const val KEY_ACTIVATION_CODE = "activation_code"
        private const val KEY_PARENTAL_UNLOCKED = "parental_unlocked"
    }
    
    fun saveActivation(code: String) {
        prefs.edit().apply {
            putBoolean(KEY_ACTIVATED, true)
            putString(KEY_ACTIVATION_CODE, code)
            apply()
        }
    }
    
    fun isActivated(): Boolean {
        return prefs.getBoolean(KEY_ACTIVATED, false)
    }
    
    fun getActivationCode(): String? {
        return prefs.getString(KEY_ACTIVATION_CODE, null)
    }
    
    fun clearActivation() {
        prefs.edit().apply {
            putBoolean(KEY_ACTIVATED, false)
            remove(KEY_ACTIVATION_CODE)
            apply()
        }
    }
    
    fun setParentalUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_PARENTAL_UNLOCKED, unlocked).apply()
    }
    
    fun isParentalUnlocked(): Boolean {
        return prefs.getBoolean(KEY_PARENTAL_UNLOCKED, false)
    }
}
