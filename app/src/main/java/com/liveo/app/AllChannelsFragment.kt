package com.liveo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChannelAdapter(
    private var channels: List<Channel>,
    private val onChannelClick: (Channel) -> Unit,
    private val onFavoriteClick: ((Channel) -> Unit)? = null,
    private val prefsManager: PreferencesManager? = null
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val channelName: TextView = view.findViewById(R.id.channelName)
        val channelImage: ImageView = view.findViewById(R.id.channelImage)
        val favoriteIcon: ImageView = view.findViewById(R.id.favoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        
        holder.channelName.text = channel.name
        
        if (channel.logo.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(channel.logo)
                .placeholder(R.drawable.ic_tv)
                .error(R.drawable.ic_tv)
                .into(holder.channelImage)
        } else {
            holder.channelImage.setImageResource(R.drawable.ic_tv)
        }
        
        // تحديث أيقونة المفضلة
        if (prefsManager != null) {
            val isFavorite = prefsManager.isFavorite(channel)
            holder.favoriteIcon.setImageResource(
                if (isFavorite) R.drawable.ic_favorite_filled 
                else R.drawable.ic_favorite_border
            )
            
            holder.favoriteIcon.setOnClickListener {
                onFavoriteClick?.invoke(channel)
            }
        } else {
            holder.favoriteIcon.visibility = View.GONE
        }
        
        holder.itemView.setOnClickListener {
            onChannelClick(channel)
        }
    }

    override fun getItemCount() = channels.size

    fun updateChannels(newChannels: List<Channel>) {
        channels = newChannels
        notifyDataSetChanged()
    }
}
