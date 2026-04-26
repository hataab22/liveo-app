package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class AllChannelsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChannelAdapter
    private lateinit var prefsManager: PreferencesManager
    
    private var allChannels = listOf<Channel>()
    private var filteredChannels = listOf<Channel>()
    
    companion object {
        private const val TAG = "AllChannelsFragment"
        
        fun newInstance(channels: List<Channel>, prefsManager: PreferencesManager): AllChannelsFragment {
            val fragment = AllChannelsFragment()
            fragment.allChannels = filterProtectedContent(channels, prefsManager)
            fragment.filteredChannels = fragment.allChannels
            fragment.prefsManager = prefsManager
            return fragment
        }
        
        private fun filterProtectedContent(channels: List<Channel>, prefsManager: PreferencesManager): List<Channel> {
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
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView = RecyclerView(requireContext())
        setupRecyclerView()
        return recyclerView
    }
    
    private fun setupRecyclerView() {
        val spanCount = when {
            resources.displayMetrics.widthPixels >= 2160 -> 6
            resources.displayMetrics.widthPixels >= 1920 -> 5
            resources.displayMetrics.widthPixels >= 1280 -> 4
            resources.displayMetrics.widthPixels >= 960 -> 3
            else -> 2
        }
        
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        
        adapter = ChannelAdapter(
            channels = filteredChannels,
            onChannelClick = { channel -> openPlayer(channel) },
            onFavoriteClick = { channel ->
                if (prefsManager.isFavorite(channel)) {
                    prefsManager.removeFromFavorites(channel)
                } else {
                    prefsManager.addToFavorites(channel)
                }
                adapter.updateChannels(filteredChannels)
            },
            isFavorite = { channel -> prefsManager.isFavorite(channel) }
        )
        
        recyclerView.adapter = adapter
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
        Log.d(TAG, "Opening player for: ${channel.name}")
        Log.d(TAG, "URL: ${channel.url}")
        
        prefsManager.addToRecent(channel)
        
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            // ✅ استخدم نفس الأسماء اللي في PlayerActivity (حرف كبير)
            putExtra("CHANNEL_NAME", channel.name)
            putExtra("CHANNEL_URL", channel.url)
            putExtra("CHANNEL_ID", channel.id)
            putExtra("all_channels", Gson().toJson(allChannels))
        }
        
        Log.d(TAG, "Starting PlayerActivity...")
        startActivity(intent)
    }
}
