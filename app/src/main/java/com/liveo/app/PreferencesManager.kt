package com.liveo.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("LiveoPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // M3U URL
    fun saveM3uUrl(url: String) {
        prefs.edit().putString("m3u_url", url).apply()
    }
    
    fun getM3uUrl(): String? {
        return prefs.getString("m3u_url", null)
    }
    
    // المفضلة
    fun saveFavorites(favorites: List<Channel>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString("favorites", json).apply()
    }
    
    fun getFavorites(): List<Channel> {
        val json = prefs.getString("favorites", "[]") ?: "[]"
        val type = object : TypeToken<List<Channel>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun addToFavorites(channel: Channel) {
        val favorites = getFavorites().toMutableList()
        if (!favorites.any { it.id == channel.id }) {
            channel.isFavorite = true
            channel.favoriteOrder = favorites.size
            favorites.add(channel)
            saveFavorites(favorites)
        }
    }
    
    fun removeFromFavorites(channelId: String) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.id == channelId }
        // إعادة ترتيب
        favorites.forEachIndexed { index, channel ->
            channel.favoriteOrder = index
        }
        saveFavorites(favorites)
    }
    
    fun reorderFavorites(fromPosition: Int, toPosition: Int) {
        val favorites = getFavorites().toMutableList()
        if (fromPosition < favorites.size && toPosition < favorites.size) {
            val item = favorites.removeAt(fromPosition)
            favorites.add(toPosition, item)
            favorites.forEachIndexed { index, channel ->
                channel.favoriteOrder = index
            }
            saveFavorites(favorites)
        }
    }
    
    // سجل المشاهدة
    fun saveWatchHistory(history: List<Channel>) {
        val json = gson.toJson(history.take(20)) // آخر 20 قناة
        prefs.edit().putString("watch_history", json).apply()
    }
    
    fun getWatchHistory(): List<Channel> {
        val json = prefs.getString("watch_history", "[]") ?: "[]"
        val type = object : TypeToken<List<Channel>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun addToWatchHistory(channel: Channel) {
        val history = getWatchHistory().toMutableList()
        // إزالة إذا كانت موجودة
        history.removeAll { it.id == channel.id }
        // إضافة في البداية
        channel.lastWatchedTime = System.currentTimeMillis()
        history.add(0, channel)
        saveWatchHistory(history.take(20))
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
        prefs.edit()
            .remove("activation_code")
            .remove("m3u_url")
            .apply()
    }
    
    fun isCodeValid(): Boolean {
    val code = getActivationCode()
    val m3uUrl = getM3uUrl()
    
    if (code == null || m3uUrl == null || !code.isActive) {
        return false
    }
    
    // إذا expiryDate = 0 (غير محدد)، نعتبر الكود صالح
    if (code.expiryDate == 0L) {
        return true
    }
    
    // إذا expiryDate محدد، نفحص التاريخ
    return code.expiryDate > System.currentTimeMillis()
}
}
