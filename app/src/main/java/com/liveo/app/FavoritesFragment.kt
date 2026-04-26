package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class FavoritesFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
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
        val view = View(requireContext())
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadFavorites()
    }
    
    private fun setupRecyclerView() {
        recyclerView = RecyclerView(requireContext())
        
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
        
        if (favorites.isEmpty()) {
            // Show empty state
            return
        }
        
        adapter = ChannelAdapter(
            channels = favorites,
            onChannelClick = { channel -> openPlayer(channel) },
            onFavoriteClick = { channel ->
                prefsManager.removeFromFavorites(channel)
                loadFavorites()
            },
            prefsManager = prefsManager
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
