package com.liveo.app

data class AppConfig(
    val version: String,
    val categories: List<Category>
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val subcategories: List<SubCategory>? = null,
    val type: String? = null
)

data class SubCategory(
    val id: String,
    val name: String,
    val icon: String,
    val group_title: String,
    val protected: Boolean = false
)
