package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AllChannelsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var categorySpinner: Spinner
    private lateinit var adapter: ChannelAdapter
    private lateinit var prefsManager: PreferencesManager
    
    private var allChannels = listOf<Channel>()
    private var filteredChannels = listOf<Channel>()
    
    companion object {
        private const val ARG_CHANNELS = "channels"
        
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
        return inflater.inflate(R.layout.fragment_channels, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.channelsRecyclerView)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        
        setupRecyclerView()
        setupCategoryFilter()
        
        filteredChannels = allChannels
        adapter.updateChannels(filteredChannels)
    }
    
    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = ChannelAdapter(
            channels = emptyList(),
            onChannelClick = { channel -> openPlayer(channel) },
            onFavoriteClick = { channel -> toggleFavorite(channel) }
        )
        recyclerView.adapter = adapter
    }
    
    private fun setupCategoryFilter() {
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
    
    private fun filterByCategory(category: String) {
        filteredChannels = if (category == "الكل") {
            allChannels
        } else {
            allChannels.filter { it.category == category }
        }
        adapter.updateChannels(filteredChannels)
    }
    
    private fun toggleFavorite(channel: Channel) {
        channel.isFavorite = !channel.isFavorite
        
        if (channel.isFavorite) {
            prefsManager.addToFavorites(channel)
            Toast.makeText(context, "تمت الإضافة للمفضلة", Toast.LENGTH_SHORT).show()
        } else {
            prefsManager.removeFromFavorites(channel.id)
            Toast.makeText(context, "تمت الإزالة من المفضلة", Toast.LENGTH_SHORT).show()
        }
        
        adapter.notifyDataSetChanged()
    }
    
    private fun openPlayer(channel: Channel) {
        prefsManager.addToWatchHistory(channel)
        
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("CHANNEL_NAME", channel.name)
            putExtra("CHANNEL_URL", channel.url)
        }
        startActivity(intent)
    }
}
