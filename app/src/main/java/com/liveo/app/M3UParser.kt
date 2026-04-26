package com.liveo.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object M3UParser {
    
    suspend fun parseFromUrl(m3uUrl: String): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        
        try {
            val connection = URL(m3uUrl).openConnection()
            connection.connectTimeout = 30000 // 30 seconds
            connection.readTimeout = 30000
            
            BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
                var currentName = ""
                var currentLogo = ""
                var currentCategory = ""
                var id = 0
                var channelCount = 0
                val maxChannels = 200 // ✅ نحمل 200 قناة بس!
                
                var line: String?
                while (reader.readLine().also { line = it } != null && channelCount < maxChannels) {
                    val currentLine = line ?: continue
                    
                    when {
                        currentLine.startsWith("#EXTINF") -> {
                            currentName = currentLine.substringAfter(",").trim()
                            currentLogo = extractAttribute(currentLine, "tvg-logo")
                            currentCategory = extractAttribute(currentLine, "group-title")
                        }
                        currentLine.startsWith("http") -> {
                            channels.add(
                                Channel(
                                    id = id.toString(),
                                    name = currentName.take(100), // ✅ نحد طول الاسم
                                    url = currentLine.trim(),
                                    logo = currentLogo.take(500), // ✅ نحد طول اللوجو URL
                                    category = currentCategory.ifEmpty { "عام" }.take(50)
                                )
                            )
                            id++
                            channelCount++
                            
                            // ✅ ننظف الذاكرة كل 50 قناة
                            if (channelCount % 50 == 0) {
                                System.gc()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        channels
    }
    
    private fun extractAttribute(line: String, attribute: String): String {
        val regex = """$attribute="([^"]*)"""".toRegex()
        return regex.find(line)?.groupValues?.getOrNull(1) ?: ""
    }
}
