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
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_RECENT = "recent"
    }
    
    fun saveActivation(code: String, adultAccess: Boolean = false) {
        prefs.edit().apply {
            putBoolean(KEY_ACTIVATED, true)
            putString(KEY_ACTIVATION_CODE, code)
            putBoolean(KEY_ADULT_ACCESS, adultAccess)
            apply()
        }
    }
    
    fun isActivated(): Boolean = prefs.getBoolean(KEY_ACTIVATED, false)
    
    fun hasAdultAccess(): Boolean = prefs.getBoolean(KEY_ADULT_ACCESS, false)
    
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
    
    fun isFavorite(channel: Channel): Boolean = getFavorites().any { it.id == channel.id }
    
    fun getFavorites(): List<Channel> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptyList() }
    }
    
    private fun saveFavorites(favorites: List<Channel>) {
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(favorites)).apply()
    }
    
    fun addToRecent(channel: Channel) {
        val recent = getRecent().toMutableList()
        recent.removeAll { it.id == channel.id }
        recent.add(0, channel)
        if (recent.size > 20) recent.removeAt(recent.size - 1)
        saveRecent(recent)
    }
    
    fun getRecent(): List<Channel> {
        val json = prefs.getString(KEY_RECENT, null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptyList() }
    }
    
    private fun saveRecent(recent: List<Channel>) {
        prefs.edit().putString(KEY_RECENT, gson.toJson(recent)).apply()
    }
}