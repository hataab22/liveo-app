package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class FavoritesFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChannelAdapter
    private lateinit var prefsManager: PreferencesManager
    
    companion object {
        fun newInstance(prefsManager: PreferencesManager): FavoritesFragment {
            val fragment = FavoritesFragment()
            fragment.prefsManager = prefsManager
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView = RecyclerView(requireContext())
        setupRecyclerView()
        loadFavorites()
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
    }
    
    private fun loadFavorites() {
        val favorites = prefsManager.getFavorites()
        
        adapter = ChannelAdapter(
            channels = favorites,
            onChannelClick = { channel -> openPlayer(channel) },
            onFavoriteClick = { channel ->
                prefsManager.removeFromFavorites(channel)
                loadFavorites()
            },
            isFavorite = { channel -> prefsManager.isFavorite(channel) }
        )
        
        recyclerView.adapter = adapter
    }
    
    fun search(query: String) {
        val favorites = prefsManager.getFavorites()
        val filtered = if (query.isEmpty()) {
            favorites
        } else {
            favorites.filter { 
                it.name.contains(query, ignoreCase = true)
            }
        }
        adapter.updateChannels(filtered)
    }
    
    private fun openPlayer(channel: Channel) {
        prefsManager.addToRecent(channel)
        
        val intent = Intent(requireContext(), PlayerActivity::class.java)
        intent.putExtra("channel_name", channel.name)
        intent.putExtra("channel_url", channel.url)
        intent.putExtra("all_channels", Gson().toJson(prefsManager.getFavorites()))
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        loadFavorites()
    }
}
