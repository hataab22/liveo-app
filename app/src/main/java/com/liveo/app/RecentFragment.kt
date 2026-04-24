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

class RecentFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: ChannelAdapter
    private lateinit var prefsManager: PreferencesManager
    
    private var recentChannels = listOf<Channel>()
    
    companion object {
        fun newInstance(prefsManager: PreferencesManager): RecentFragment {
            val fragment = RecentFragment()
            fragment.prefsManager = prefsManager
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recent, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recentRecyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        
        setupRecyclerView()
        loadRecent()
    }
    
    override fun onResume() {
        super.onResume()
        loadRecent()
    }
    
    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = ChannelAdapter(
            channels = emptyList(),
            onChannelClick = { channel -> openPlayer(channel) }
        )
        recyclerView.adapter = adapter
    }
    
    private fun loadRecent() {
        recentChannels = prefsManager.getWatchHistory()
        
        if (recentChannels.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            adapter.updateChannels(recentChannels)
        }
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
