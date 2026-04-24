package com.liveo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class FavoritesFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: ChannelAdapter
    private lateinit var prefsManager: PreferencesManager
    
    private var favorites = listOf<Channel>()
    
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
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        
        setupRecyclerView()
        loadFavorites()
    }
    
    override fun onResume() {
        super.onResume()
        loadFavorites()
    }
    
    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = ChannelAdapter(
            channels = emptyList(),
            onChannelClick = { channel -> openPlayer(channel) },
            onFavoriteClick = { channel -> removeFavorite(channel) }
        )
        recyclerView.adapter = adapter
        
        // إعادة الترتيب بالسحب
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or 
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                
                prefsManager.reorderFavorites(fromPosition, toPosition)
                loadFavorites()
                
                return true
            }
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    
    private fun loadFavorites() {
        favorites = prefsManager.getFavorites()
        
        if (favorites.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            adapter.updateChannels(favorites)
        }
    }
    
    private fun removeFavorite(channel: Channel) {
        prefsManager.removeFromFavorites(channel.id)
        Toast.makeText(context, "تمت الإزالة من المفضلة", Toast.LENGTH_SHORT).show()
        loadFavorites()
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
