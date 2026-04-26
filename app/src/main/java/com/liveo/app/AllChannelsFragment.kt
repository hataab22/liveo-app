package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class AllChannelsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var categorySpinner: Spinner
    private lateinit var adapter: ChannelAdapter
    private lateinit var prefsManager: PreferencesManager
    
    private var allChannels = listOf<Channel>()
    private var filteredChannels = listOf<Channel>()
    
    companion object {
        fun newInstance(channels: List<Channel>, prefsManager: PreferencesManager): AllChannelsFragment {
            val fragment = AllChannelsFragment()
            fragment.allChannels = channels
            fragment.prefsManager = prefsManager
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_channels, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        
        setupRecyclerView()
        setupCategoryFilter()
        
        return view
    }
    
    private fun setupRecyclerView() {
        val spanCount = when {
            resources.displayMetrics.widthPixels >= 2160 -> 6  // TV 4K
            resources.displayMetrics.widthPixels >= 1920 -> 5  // TV FHD
            resources.displayMetrics.widthPixels >= 1280 -> 4  // Tablet landscape
            resources.displayMetrics.widthPixels >= 960 -> 3   // Tablet
            else -> 2  // Phone
        }
        
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        
        adapter = ChannelAdapter(filteredChannels) { channel ->
            openPlayer(channel)
        }
        
        recyclerView.adapter = adapter
    }
    
    private fun setupCategoryFilter() {
        // فلترة المحتوى المحمي
        allChannels = filterProtectedContent(allChannels)
        
        val categories = mutableListOf("الكل")
        categories.addAll(allChannels.map { it.category }.distinct().sorted())
        
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter
        
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterByCategory(categories[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun filterProtectedContent(channels: List<Channel>): List<Channel> {
        val isUnlocked = prefsManager.isParentalUnlocked()
        
        return if (isUnlocked) {
            channels
        } else {
            channels.filter { channel ->
                !channel.category.contains("+18", ignoreCase = true) &&
                !channel.category.contains("adult", ignoreCase = true) &&
                !channel.category.contains("للكبار", ignoreCase = true)
            }
        }
    }
    
    private fun filterByCategory(category: String) {
        filteredChannels = if (category == "الكل") {
            allChannels
        } else {
            allChannels.filter { it.category == category }
        }
        adapter.updateChannels(filteredChannels)
    }
    
    fun search(query: String) {
        filteredChannels = if (query.isEmpty()) {
            allChannels
        } else {
            allChannels.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.category.contains(query, ignoreCase = true)
            }
        }
        adapter.updateChannels(filteredChannels)
    }
    
    private fun openPlayer(channel: Channel) {
        // حفظ في الأخيرة
        prefsManager.addToRecent(channel)
        
        val intent = Intent(requireContext(), PlayerActivity::class.java)
        intent.putExtra("channel_name", channel.name)
        intent.putExtra("channel_url", channel.url)
        intent.putExtra("all_channels", Gson().toJson(allChannels))
        startActivity(intent)
    }
}
