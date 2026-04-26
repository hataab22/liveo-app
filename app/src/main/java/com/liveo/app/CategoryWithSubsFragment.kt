package com.liveo.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryWithSubsFragment : Fragment() {
    
    private lateinit var mainLayout: LinearLayout
    private lateinit var subCategoriesLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChannelAdapter
    private lateinit var prefsManager: PreferencesManager
    
    private var allChannels = listOf<Channel>()
    private var filteredChannels = listOf<Channel>()
    private var categoryType = ""
    private var selectedSubCategory = "الكل"
    
    companion object {
        private const val TAG = "CategoryWithSubs"
        
        fun newInstance(
            channels: List<Channel>,
            prefsManager: PreferencesManager,
            categoryType: String
        ): CategoryWithSubsFragment {
            val fragment = CategoryWithSubsFragment()
            fragment.allChannels = channels
            fragment.prefsManager = prefsManager
            fragment.categoryType = categoryType
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#121212"))
        }
        
        setupSubCategories()
        setupRecyclerView()
        
        mainLayout.addView(createSubCategoriesScrollView())
        mainLayout.addView(recyclerView)
        
        filterBySubCategory("الكل")
        
        return mainLayout
    }
    
    private fun createSubCategoriesScrollView(): HorizontalScrollView {
        subCategoriesLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16)
        }
        
        return HorizontalScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            addView(subCategoriesLayout)
        }
    }
    
    private fun setupSubCategories() {
        val subCategories = when (categoryType) {
            "بث مباشر" -> listOf("الكل", "رياضة", "أخبار", "وثائقيات", "MBC", "OSN", "أطفال")
            "أفلام" -> listOf("الكل", "عربي", "أجنبي", "مصري", "أكشن", "كوميدي", "للكبار")
            "مسلسلات" -> listOf("الكل", "عربي", "تركي", "هندي", "أجنبي", "كوري")
            "موسيقى" -> listOf("الكل", "عربي", "أجنبي")
            else -> listOf("الكل")
        }
        
        subCategoriesLayout.removeAllViews()
        
        subCategories.forEach { subCat ->
            val button = createSubCategoryButton(subCat)
            subCategoriesLayout.addView(button)
        }
    }
    
    private fun createSubCategoryButton(subCategory: String): TextView {
        return TextView(requireContext()).apply {
            text = subCategory
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(32, 16, 32, 16)
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            
            setBackgroundColor(
                if (subCategory == selectedSubCategory) 
                    Color.parseColor("#9C27B0") 
                else 
                    Color.parseColor("#2E2E2E")
            )
            
            setOnClickListener {
                selectedSubCategory = subCategory
                filterBySubCategory(subCategory)
                setupSubCategories()
            }
        }
    }
    
    private fun filterBySubCategory(subCategory: String) {
        Log.d(TAG, "Filtering $categoryType by: $subCategory")
        
        filteredChannels = when {
            subCategory == "الكل" -> {
                if (prefsManager.isParentalUnlocked()) {
                    allChannels
                } else {
                    allChannels.filter { !isAdultContent(it) }
                }
            }
            
            categoryType == "بث مباشر" && subCategory == "رياضة" -> {
                allChannels.filter { 
                    (it.category.contains("رياضة", true) || it.category.contains("sport", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "بث مباشر" && subCategory == "أخبار" -> {
                allChannels.filter { 
                    (it.category.contains("أخبار", true) || it.category.contains("news", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "بث مباشر" && subCategory == "وثائقيات" -> {
                allChannels.filter { 
                    (it.category.contains("وثائق", true) || it.category.contains("document", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "بث مباشر" && subCategory == "MBC" -> {
                allChannels.filter { 
                    (it.category.contains("MBC", true) || it.name.contains("MBC", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "بث مباشر" && subCategory == "OSN" -> {
                allChannels.filter { 
                    (it.category.contains("OSN", true) || it.name.contains("OSN", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "بث مباشر" && subCategory == "أطفال" -> {
                allChannels.filter { 
                    it.category.contains("أطفال", true) || it.category.contains("kids", true)
                }
            }
            
            categoryType == "أفلام" && subCategory == "عربي" -> {
                allChannels.filter { 
                    (it.category.contains("عربي", true) || it.category.contains("arabic", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "أفلام" && subCategory == "أجنبي" -> {
                allChannels.filter { 
                    (it.category.contains("أجنبي", true) || it.category.contains("foreign", true) || it.category.contains("hollywood", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "أفلام" && subCategory == "مصري" -> {
                allChannels.filter { 
                    (it.category.contains("مصري", true) || it.category.contains("egypt", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "أفلام" && subCategory == "أكشن" -> {
                allChannels.filter { 
                    (it.category.contains("أكشن", true) || it.category.contains("action", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "أفلام" && subCategory == "كوميدي" -> {
                allChannels.filter { 
                    (it.category.contains("كوميدي", true) || it.category.contains("comedy", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "أفلام" && subCategory == "للكبار" -> {
                if (prefsManager.isParentalUnlocked()) {
                    allChannels.filter { isAdultContent(it) }
                } else {
                    emptyList()
                }
            }
            
            categoryType == "مسلسلات" && subCategory == "عربي" -> {
                allChannels.filter { 
                    it.category.contains("عربي", true) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "مسلسلات" && subCategory == "تركي" -> {
                allChannels.filter { 
                    (it.category.contains("تركي", true) || it.category.contains("turkish", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "مسلسلات" && subCategory == "هندي" -> {
                allChannels.filter { 
                    (it.category.contains("هندي", true) || it.category.contains("hindi", true) || it.category.contains("bollywood", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "مسلسلات" && subCategory == "أجنبي" -> {
                allChannels.filter { 
                    it.category.contains("أجنبي", true) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            categoryType == "مسلسلات" && subCategory == "كوري" -> {
                allChannels.filter { 
                    (it.category.contains("كوري", true) || it.category.contains("korean", true)) &&
                    (prefsManager.isParentalUnlocked() || !isAdultContent(it))
                }
            }
            
            categoryType == "موسيقى" && subCategory == "عربي" -> {
                allChannels.filter { it.category.contains("عربي", true) }
            }
            categoryType == "موسيقى" && subCategory == "أجنبي" -> {
                allChannels.filter { it.category.contains("أجنبي", true) }
            }
            
            else -> {
                if (prefsManager.isParentalUnlocked()) {
                    allChannels
                } else {
                    allChannels.filter { !isAdultContent(it) }
                }
            }
        }
        
        Log.d(TAG, "Filtered: ${filteredChannels.size} channels")
        adapter.updateChannels(filteredChannels)
    }
    
    private fun isAdultContent(channel: Channel): Boolean {
        val category = channel.category.lowercase()
        val name = channel.name.lowercase()
        return category.contains("+18") ||
               category.contains("adult") ||
               category.contains("للكبار") ||
               name.contains("+18")
    }
    
    private fun setupRecyclerView() {
        val spanCount = when {
            resources.displayMetrics.widthPixels >= 2160 -> 6
            resources.displayMetrics.widthPixels >= 1920 -> 5
            resources.displayMetrics.widthPixels >= 1280 -> 4
            resources.displayMetrics.widthPixels >= 960 -> 3
            else -> 2
        }
        
        recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            layoutManager = GridLayoutManager(requireContext(), spanCount)
        }
        
        adapter = ChannelAdapter(
            channels = filteredChannels,
            onChannelClick = { channel -> openPlayer(channel) },
            onFavoriteClick = { channel ->
                if (prefsManager.isFavorite(channel)) {
                    prefsManager.removeFromFavorites(channel)
                } else {
                    prefsManager.addToFavorites(channel)
                }
                adapter.notifyDataSetChanged()
            },
            isFavorite = { channel -> prefsManager.isFavorite(channel) }
        )
        
        recyclerView.adapter = adapter
    }
    
    private fun openPlayer(channel: Channel) {
        prefsManager.addToRecent(channel)
        
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra("CHANNEL_NAME", channel.name)
            putExtra("CHANNEL_URL", channel.url)
            putExtra("CHANNEL_ID", channel.id)
        }
        
        startActivity(intent)
    }
}
