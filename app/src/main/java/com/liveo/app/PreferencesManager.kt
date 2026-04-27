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
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_RECENT = "recent"
    }
    
    // Activation
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
    
    // Parental Control
    fun setParentalUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_PARENTAL_UNLOCKED, unlocked).apply()
    }
    
    fun isParentalUnlocked(): Boolean {
        return prefs.getBoolean(KEY_PARENTAL_UNLOCKED, false)
    }
    
    // Favorites
    fun addToFavorites(channel: Channel) {
        val favorites = getFavorites().toMutableList()
        if (!favorites.any { it.id == channel.id }) {
            favorites.add(channel)
            saveFavorites(favorites)
        }
    }
    
    fun removeFromFavorites(channel: Channel) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.id == channel.id }
        saveFavorites(favorites)
    }
    
    fun isFavorite(channel: Channel): Boolean {
        return getFavorites().any { it.id == channel.id }
    }
    
    fun getFavorites(): List<Channel> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun saveFavorites(favorites: List<Channel>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }
    
    // Recent
    fun addToRecent(channel: Channel) {
        val recent = getRecent().toMutableList()
        recent.removeAll { it.id == channel.id }
        recent.add(0, channel)
        if (recent.size > 20) {
            recent.removeAt(recent.size - 1)
        }
        saveRecent(recent)
    }
    
    fun getRecent(): List<Channel> {
        val json = prefs.getString(KEY_RECENT, null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun saveRecent(recent: List<Channel>) {
        val json = gson.toJson(recent)
        prefs.edit().putString(KEY_RECENT, json).apply()
    }
}
