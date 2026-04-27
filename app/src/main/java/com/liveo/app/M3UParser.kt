package com.liveo.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class M3UParser(private val url: String) {
    
    suspend fun parse(): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        
        try {
            val content = URL(url).readText()
            val lines = content.lines()
            
            var i = 0
            while (i < lines.size) {
                val line = lines[i].trim()
                
                if (line.startsWith("#EXTINF:")) {
                    val name = extractName(line)
                    val logo = extractLogo(line)
                    val category = extractCategory(line)
                    
                    if (i + 1 < lines.size) {
                        val streamUrl = lines[i + 1].trim()
                        if (streamUrl.isNotEmpty() && !streamUrl.startsWith("#")) {
                            channels.add(Channel(name, streamUrl, logo, category))
                        }
                    }
                    i += 2
                } else {
                    i++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        channels
    }
    
    private fun extractName(line: String): String {
        val nameStart = line.lastIndexOf(',')
        return if (nameStart != -1 && nameStart < line.length - 1) {
            line.substring(nameStart + 1).trim()
        } else {
            "Unknown"
        }
    }
    
    private fun extractLogo(line: String): String {
        val logoPattern = """tvg-logo="([^"]*)"""".toRegex()
        return logoPattern.find(line)?.groupValues?.get(1) ?: ""
    }
    
    private fun extractCategory(line: String): String {
        val groupPattern = """group-title="([^"]*)"""".toRegex()
        return groupPattern.find(line)?.groupValues?.get(1) ?: ""
    }
}