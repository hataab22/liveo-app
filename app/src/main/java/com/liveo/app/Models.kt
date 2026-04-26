package com.liveo.app

data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val logo: String,
    val category: String,
    var isFavorite: Boolean = false,
    var favoriteOrder: Int = 0,
    var lastWatchedTime: Long = 0L
) {
    val categoryType: CategoryType
        get() = when {
            category.contains("movie", ignoreCase = true) || 
            category.contains("film", ignoreCase = true) ||
            category.contains("أفلام", ignoreCase = true) -> CategoryType.MOVIES
            
            category.contains("series", ignoreCase = true) || 
            category.contains("show", ignoreCase = true) ||
            category.contains("مسلسل", ignoreCase = true) -> CategoryType.SERIES
            
            category.contains("music", ignoreCase = true) ||
            category.contains("موسيقى", ignoreCase = true) -> CategoryType.MUSIC
            
            else -> CategoryType.LIVE_TV
        }
}

enum class CategoryType(val arabicName: String, val icon: String) {
    LIVE_TV("بث مباشر", "📺"),
    MOVIES("أفلام", "🎬"),
    SERIES("مسلسلات", "📺"),
    MUSIC("موسيقى", "🎵"),
    RECENT("المشاهدة الأخيرة", "⏱️"),
    FAVORITES("المفضلة", "⭐")
}

data class ActivationCode(
    val code: String,
    val expiryDate: Long,
    val isActive: Boolean,
    val customerName: String = "",
    val parentalPin: String? = null
)

data class ActivationResponse(
    val success: Boolean,
    val code: String? = null,
    val m3u_url: String? = null,
    val expires_at: Long? = null,
    val customer_name: String? = null,
    val message: String? = null,
    val parental_pin: String? = null
)

data class PinVerificationResponse(
    val success: Boolean,
    val valid: Boolean = false,
    val message: String? = null
)
