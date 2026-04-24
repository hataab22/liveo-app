package com.liveo.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object M3UParser {
    
    suspend fun parseFromUrl(m3uUrl: String): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        
        try {
            val content = URL(m3uUrl).readText()
            val lines = content.split("\n")
            
            var currentName = ""
            var currentLogo = ""
            var currentCategory = ""
            var id = 0
            
            for (line in lines) {
                when {
                    line.startsWith("#EXTINF") -> {
                        currentName = line.substringAfter(",").trim()
                        currentLogo = extractAttribute(line, "tvg-logo")
                        currentCategory = extractAttribute(line, "group-title")
                    }
                    line.startsWith("http") -> {
                        channels.add(
                            Channel(
                                id = id.toString(),
                                name = currentName,
                                url = line.trim(),
                                logo = currentLogo,
                                category = currentCategory.ifEmpty { "عام" }
                            )
                        )
                        id++
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
        return regex.find(line)?.groupValues?.get(1) ?: ""
    }
}
