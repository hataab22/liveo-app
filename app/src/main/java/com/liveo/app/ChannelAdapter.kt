package com.liveo.app

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChannelAdapter(
    private var channels: List<Channel>,
    private val onChannelClick: (Channel) -> Unit,
    private val onFavoriteClick: ((Channel) -> Unit)? = null,
    private val isFavorite: ((Channel) -> Boolean)? = null
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    companion object {
        private const val TAG = "ChannelAdapter"
    }

    class ChannelViewHolder(val container: FrameLayout) : RecyclerView.ViewHolder(container) {
        val channelImage: ImageView
        val channelName: TextView
        val favoriteIcon: ImageView

        init {
            val cardLayout = LinearLayout(container.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                setBackgroundColor(Color.parseColor("#1E1E1E"))
                setPadding(16, 16, 16, 16)
            }

            channelImage = ImageView(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300
                ).apply {
                    bottomMargin = 12
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.parseColor("#2E2E2E"))
            }

            channelName = TextView(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 14f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                maxLines = 2
            }

            favoriteIcon = ImageView(container.context).apply {
                layoutParams = FrameLayout.LayoutParams(48, 48).apply {
                    gravity = Gravity.TOP or Gravity.END
                    setMargins(8, 8, 8, 8)
                }
                setColorFilter(Color.parseColor("#FF6B6B"))
            }

            cardLayout.addView(channelImage)
            cardLayout.addView(channelName)
            container.addView(cardLayout)
            container.addView(favoriteIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val container = FrameLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ChannelViewHolder(container)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        
        holder.channelName.text = channel.name
        
        if (channel.logo.isNotEmpty()) {
            Glide.with(holder.container.context)
                .load(channel.logo)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.channelImage)
        } else {
            holder.channelImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        
        if (isFavorite != null && onFavoriteClick != null) {
            val isFav = isFavorite.invoke(channel)
            holder.favoriteIcon.setImageResource(
                if (isFav) android.R.drawable.star_big_on
                else android.R.drawable.star_big_off
            )
            holder.favoriteIcon.setOnClickListener {
                onFavoriteClick.invoke(channel)
            }
        } else {
            holder.favoriteIcon.visibility = android.view.View.GONE
        }
        
        holder.container.setOnClickListener {
            Log.d(TAG, "=================================")
            Log.d(TAG, "Channel clicked: ${channel.name}")
            Log.d(TAG, "Channel URL: ${channel.url}")
            Log.d(TAG, "Channel ID: ${channel.id}")
            Log.d(TAG, "=================================")
            
            // ✅ نفتح المشغل مباشرة
            val context = holder.container.context
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra("CHANNEL_NAME", channel.name)
                putExtra("CHANNEL_URL", channel.url)
                putExtra("CHANNEL_ID", channel.id)
            }
            
            Log.d(TAG, "Starting PlayerActivity...")
            context.startActivity(intent)
            
            // ✅ نستدعي الـ callback كمان (للمفضلة والأخيرة)
            onChannelClick(channel)
        }
    }

    override fun getItemCount() = channels.size

    fun updateChannels(newChannels: List<Channel>) {
        channels = newChannels
        notifyDataSetChanged()
    }
}
