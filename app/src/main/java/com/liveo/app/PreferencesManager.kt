package com.liveo.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("liveo_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // القنوات المفضلة
    fun getFavorites(): List<Channel> {
        val json = prefs.getString("favorites", null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return gson.fromJson(json, type)
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
    
    private fun saveFavorites(favorites: List<Channel>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString("favorites", json).apply()
    }
    
    fun isFavorite(channel: Channel): Boolean {
        return getFavorites().any { it.id == channel.id }
    }
    
    // القنوات الأخيرة
    fun getRecent(): List<Channel> {
        val json = prefs.getString("recent", null) ?: return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun addToRecent(channel: Channel) {
        val recent = getRecent().toMutableList()
        recent.removeAll { it.id == channel.id }
        recent.add(0, channel)
        if (recent.size > 20) {
            recent.removeAt(recent.size - 1)
        }
        saveRecent(recent)
    }
    
    private fun saveRecent(recent: List<Channel>) {
        val json = gson.toJson(recent)
        prefs.edit().putString("recent", json).apply()
    }
    
    // كود التفعيل
    fun saveActivationCode(code: ActivationCode) {
        val json = gson.toJson(code)
        prefs.edit().putString("activation_code", json).apply()
    }
    
    fun getActivationCode(): ActivationCode? {
        val json = prefs.getString("activation_code", null)
        return if (json != null) {
            gson.fromJson(json, ActivationCode::class.java)
        } else null
    }
    
    fun clearActivationCode() {
        prefs.edit().remove("activation_code").apply()
    }
    
    fun isCodeValid(): Boolean {
        val code = getActivationCode()
        return code != null && 
               code.isActive && 
               code.expiryDate > System.currentTimeMillis()
    }
    
    // دوال الحماية
    fun isParentalUnlocked(): Boolean {
        return prefs.getBoolean("parental_unlocked", false)
    }
    
    fun setParentalUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean("parental_unlocked", unlocked).apply()
    }
    
    fun resetParentalLock() {
        setParentalUnlocked(false)
    }
    
    fun hasParentalPin(): Boolean {
        return getActivationCode()?.parentalPin?.isNotEmpty() == true
    }
    
    fun getParentalPin(): String? {
        return getActivationCode()?.parentalPin
    }
}
