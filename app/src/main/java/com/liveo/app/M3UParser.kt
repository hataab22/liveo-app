package com.liveo.app

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object M3UParser {
    
    private const val TAG = "M3UParser"
    
    suspend fun parseFromUrl(m3uUrl: String): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        
        try {
            Log.d(TAG, "Parsing: $m3uUrl")
            
            val connection = URL(m3uUrl).openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            
            BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
                var currentName = ""
                var currentLogo = ""
                var currentCategory = ""
                var id = 0
                
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    
                    try {
                        when {
                            currentLine.startsWith("#EXTINF") -> {
                                currentName = currentLine.substringAfter(",", "").trim()
                                currentLogo = extractAttribute(currentLine, "tvg-logo")
                                currentCategory = extractAttribute(currentLine, "group-title")
                            }
                            currentLine.trim().startsWith("http") -> {
                                // ✅ تجاهل الروابط الوهمية والفارغة
                                if (currentLine.trim() != "ضع_الرابط_هنا" && 
                                    !currentLine.contains("ضع_الرابط") &&
                                    currentLine.trim().isNotEmpty()) {
                                    
                                    val channel = Channel(
                                        id = id.toString(),
                                        name = if (currentName.isNotEmpty()) currentName else "قناة $id",
                                        url = currentLine.trim(),
                                        logo = currentLogo,
                                        category = currentCategory.ifEmpty { "عام" }
                                    )
                                    channels.add(channel)
                                    
                                    if (id % 10 == 0) {
                                        Log.d(TAG, "Loaded $id channels...")
                                    }
                                    
                                    id++
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing line", e)
                    }
                }
            }
            
            Log.d(TAG, "Total channels parsed: ${channels.size}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing M3U", e)
        }
        
        channels
    }
    
    private fun extractAttribute(line: String, attribute: String): String {
        return try {
            val regex = """$attribute="([^"]*)"""".toRegex()
            regex.find(line)?.groupValues?.getOrNull(1) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
