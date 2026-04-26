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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryWithSubsFragment : Fragment() {
    
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
        val mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#121212"))
        }
        
        mainLayout.addView(createSubCategoriesView())
        setupRecyclerView()
        mainLayout.addView(recyclerView)
        filterBySubCategory("الكل")
        
        return mainLayout
    }
    
    private fun createSubCategoriesView(): HorizontalScrollView {
        val subCategories = when (categoryType) {
            "بث مباشر" -> {
                val base = mutableListOf("الكل", "رياضة", "أخبار", "MBC", "أطفال")
                if (prefsManager.isParentalUnlocked()) {
                    base.add("للكبار")
                }
                base.toList()
            }
            "أفلام" -> {
                val base = mutableListOf("الكل", "عربي", "أجنبي")
                if (prefsManager.isParentalUnlocked()) {
                    base.add("للكبار")
                }
                base.toList()
            }
            "مسلسلات" -> listOf("الكل", "عربي", "تركي", "هندي")
            "موسيقى" -> listOf("الكل", "عربي", "أجنبي")
            else -> listOf("الكل")
        }
        
        val buttonsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            val padding = 16
            setPadding(padding, padding, padding, padding)
        }
        
        subCategories.forEach { subCat ->
            buttonsLayout.addView(createButton(subCat))
        }
        
        return HorizontalScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            addView(buttonsLayout)
        }
    }
    
    private fun createButton(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            val padding = 24
            setPadding(padding * 2, padding, padding * 2, padding)
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 8, 8, 8)
            layoutParams = params
            
            setBackgroundColor(
                if (text == selectedSubCategory) 
                    Color.parseColor("#9C27B0") 
                else 
                    Color.parseColor("#2E2E2E")
            )
            
            setOnClickListener {
                selectedSubCategory = text
                filterBySubCategory(text)
                (parent as? ViewGroup)?.let { parent ->
                    for (i in 0 until parent.childCount) {
                        (parent.getChildAt(i) as? TextView)?.setBackgroundColor(
                            Color.parseColor("#2E2E2E")
                        )
                    }
                }
                setBackgroundColor(Color.parseColor("#9C27B0"))
            }
        }
    }
    
    private fun filterBySubCategory(subCategory: String) {
        filteredChannels = when {
            subCategory == "الكل" -> {
                if (prefsManager.isParentalUnlocked()) allChannels
                else allChannels.filter { !isAdult(it) }
            }
            subCategory == "رياضة" -> allChannels.filter { 
                it.category.contains("رياضة", true) || it.category.contains("sport", true)
            }
            subCategory == "أخبار" -> allChannels.filter { 
                it.category.contains("أخبار", true) || it.category.contains("news", true)
            }
            subCategory == "MBC" -> allChannels.filter { 
                it.category.contains("MBC", true) || it.name.contains("MBC", true)
            }
            subCategory == "أطفال" -> allChannels.filter { 
                it.category.contains("أطفال", true) || it.category.contains("kids", true)
            }
            subCategory == "عربي" -> allChannels.filter { 
                it.category.contains("عربي", true)
            }
            subCategory == "أجنبي" -> allChannels.filter { 
                it.category.contains("أجنبي", true)
            }
            subCategory == "تركي" -> allChannels.filter { 
                it.category.contains("تركي", true) || it.category.contains("turkish", true)
            }
            subCategory == "هندي" -> allChannels.filter { 
                it.category.contains("هندي", true) || it.category.contains("hindi", true)
            }
            subCategory == "للكبار" -> {
                if (prefsManager.isParentalUnlocked()) 
                    allChannels.filter { isAdult(it) }
                else emptyList()
            }
            else -> allChannels
        }
        
        Log.d(TAG, "$categoryType -> $subCategory: ${filteredChannels.size} channels")
        adapter.updateChannels(filteredChannels)
    }
    
    private fun isAdult(channel: Channel): Boolean {
        val cat = channel.category.lowercase()
        return cat.contains("+18") || cat.contains("adult") || cat.contains("للكبار")
    }
    
    private fun setupRecyclerView() {
        val spanCount = when {
            resources.displayMetrics.widthPixels >= 1920 -> 4
            resources.displayMetrics.widthPixels >= 1280 -> 3
            else -> 3
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
            onChannelClick = { channel ->
                prefsManager.addToRecent(channel)
                val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra("CHANNEL_NAME", channel.name)
                    putExtra("CHANNEL_URL", channel.url)
                    putExtra("CHANNEL_ID", channel.id)
                }
                startActivity(intent)
            },
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
}
