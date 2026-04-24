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
)

data class ActivationCode(
    val code: String,
    val expiryDate: Long,
    val isActive: Boolean,
    val customerName: String = ""
)
