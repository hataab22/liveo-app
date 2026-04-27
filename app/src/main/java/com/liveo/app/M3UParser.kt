package com.liveo.app

import java.net.URL

class M3UParser(private val url: String) {
    
    fun parse(): List<Channel> {
        val channels = mutableListOf<Channel>()
        
        try {
            val content = URL(url).readText()
            val lines = content.split("\n")
            
            var currentName = ""
            var currentLogo = ""
            var currentCategory = ""
            var isAdult = false
            
            for (line in lines) {
                when {
                    line.startsWith("#EXTINF:") -> {
                        // استخرج المعلومات
                        currentLogo = extractLogo(line)
                        currentCategory = extractCategory(line)
                        currentName = extractName(line)
                        
                        // تحقق إذا القناة +18
                        isAdult = currentCategory.contains("+18", ignoreCase = true) ||
                                 currentName.contains("+18", ignoreCase = true)
                    }
                    line.startsWith("http") -> {
                        if (currentName.isNotEmpty()) {
                            channels.add(
                                Channel(
                                    name = currentName,
                                    url = line.trim(),
                                    logo = currentLogo,
                                    category = currentCategory,
                                    isAdult = isAdult
                                )
                            )
                        }
                        currentName = ""
                        currentLogo = ""
                        currentCategory = ""
                        isAdult = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return channels
    }
    
    private fun extractLogo(line: String): String {
        val logoPattern = """tvg-logo="([^"]*)"""".toRegex()
        return logoPattern.find(line)?.groupValues?.get(1) ?: ""
    }
    
    private fun extractCategory(line: String): String {
        val categoryPattern = """group-title="([^"]*)"""".toRegex()
        return categoryPattern.find(line)?.groupValues?.get(1) ?: ""
    }
    
    private fun extractName(line: String): String {
        val parts = line.split(",")
        return if (parts.size > 1) parts.last().trim() else ""
    }
}
