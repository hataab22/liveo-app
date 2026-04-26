package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
        return recyclerView
    }
    
    override fun onResume() {
        super.onResume()
        adapter.updateChannels(prefsManager.getFavorites())
    }
    
    private fun setupRecyclerView() {
        val spanCount = when {
            resources.displayMetrics.widthPixels >= 1920 -> 4
            resources.displayMetrics.widthPixels >= 1280 -> 3
            else -> 3
        }
        
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        
        adapter = ChannelAdapter(
            channels = prefsManager.getFavorites(),
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
                prefsManager.removeFromFavorites(channel)
                adapter.updateChannels(prefsManager.getFavorites())
            },
            isFavorite = { true }
        )
        
        recyclerView.adapter = adapter
    }
}
