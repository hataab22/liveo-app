package com.liveo.app

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var channelNameText: TextView
    
    private var channelName: String = ""
    private var channelUrl: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        
        supportActionBar?.hide()
        
        channelName = intent.getStringExtra("CHANNEL_NAME") ?: ""
        channelUrl = intent.getStringExtra("CHANNEL_URL") ?: ""
        
        setupViews()
        initializePlayer()
    }
    
    private fun setupViews() {
        playerView = findViewById(R.id.playerView)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        channelNameText = findViewById(R.id.channelNameText)
        
        channelNameText.text = channelName
    }
    
    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer
            
            val mediaItem = MediaItem.fromUri(channelUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            loadingIndicator.visibility = View.VISIBLE
                        }
                        Player.STATE_READY -> {
                            loadingIndicator.visibility = View.GONE
                        }
                        Player.STATE_ENDED -> {
                            loadingIndicator.visibility = View.GONE
                        }
                        Player.STATE_IDLE -> {
                            loadingIndicator.visibility = View.GONE
                        }
                    }
                }
                
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    loadingIndicator.visibility = View.GONE
                    Toast.makeText(
                        this@PlayerActivity,
                        "خطأ في التشغيل: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }
    
    override fun onStop() {
        super.onStop()
        releasePlayer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
    
    private fun releasePlayer() {
        player?.release()
        player = null
    }
}
