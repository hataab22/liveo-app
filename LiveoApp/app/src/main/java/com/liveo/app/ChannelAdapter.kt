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
    private val onFavoriteClick: ((Channel) -> Unit)? = null
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo: ImageView = itemView.findViewById(R.id.channelLogo)
        val name: TextView = itemView.findViewById(R.id.channelName)
        val category: TextView = itemView.findViewById(R.id.channelCategory)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        
        holder.name.text = channel.name
        holder.category.text = channel.category
        
        // الشعار
        if (channel.logo.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(channel.logo)
                .placeholder(R.drawable.ic_tv_placeholder)
                .error(R.drawable.ic_tv_placeholder)
                .into(holder.logo)
        } else {
            holder.logo.setImageResource(R.drawable.ic_tv_placeholder)
        }
        
        // أيقونة المفضلة
        if (onFavoriteClick != null) {
            holder.favoriteIcon.visibility = View.VISIBLE
            holder.favoriteIcon.setImageResource(
                if (channel.isFavorite) R.drawable.ic_favorite_filled 
                else R.drawable.ic_favorite_outline
            )
            
            holder.favoriteIcon.setOnClickListener {
                onFavoriteClick.invoke(channel)
            }
        } else {
            holder.favoriteIcon.visibility = View.GONE
        }
        
        // الضغط على القناة
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
