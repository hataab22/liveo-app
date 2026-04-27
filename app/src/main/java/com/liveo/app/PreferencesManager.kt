package com.liveo.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("liveo_prefs", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    fun saveActivationCode(code: String) {
        sharedPreferences.edit().putString("activation_code", code).apply()
    }
    
    fun getActivationCode(): String? {
        return sharedPreferences.getString("activation_code", null)
    }
    
    fun isActivated(): Boolean {
        return sharedPreferences.getBoolean("activated", false)
    }
    
    fun setActivated(activated: Boolean) {
        sharedPreferences.edit().putBoolean("activated", activated).apply()
    }
    
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
        val json = sharedPreferences.getString("favorites", null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return gson.fromJson(json, type)
    }
    
    private fun saveFavorites(favorites: List<Channel>) {
        val json = gson.toJson(favorites)
        sharedPreferences.edit().putString("favorites", json).apply()
    }
    
    fun addToRecent(channel: Channel) {
        val recent = getRecent().toMutableList()
        recent.removeAll { it.id == channel.id }
        recent.add(0, channel)
        if (recent.size > 50) {
            recent.removeAt(recent.size - 1)
        }
        saveRecent(recent)
    }
    
    fun getRecent(): List<Channel> {
        val json = sharedPreferences.getString("recent", null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return gson.fromJson(json, type)
    }
    
    private fun saveRecent(recent: List<Channel>) {
        val json = gson.toJson(recent)
        sharedPreferences.edit().putString("recent", json).apply()
    }
    
    fun isParentalUnlocked(): Boolean {
        return sharedPreferences.getBoolean("parental_unlocked", false)
    }
    
    fun setParentalUnlocked(unlocked: Boolean) {
        sharedPreferences.edit().putBoolean("parental_unlocked", unlocked).apply()
    }
    
    fun clearActivation() {
        sharedPreferences.edit()
            .remove("activation_code")
            .remove("activated")
            .apply()
    }
}
