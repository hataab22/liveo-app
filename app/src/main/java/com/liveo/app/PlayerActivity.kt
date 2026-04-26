package com.liveo.app

import android.os.Bundle
import android.util.Log
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
    
    companion object {
        private const val TAG = "PlayerActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        
        supportActionBar?.hide()
        
        channelName = intent.getStringExtra("CHANNEL_NAME") ?: ""
        channelUrl = intent.getStringExtra("CHANNEL_URL") ?: ""
        
        Log.d(TAG, "Channel: $channelName")
        Log.d(TAG, "URL: $channelUrl")
        
        if (channelUrl.isEmpty()) {
            Toast.makeText(this, "لا يوجد رابط للقناة", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Empty URL!")
            finish()
            return
        }
        
        setupViews()
        initializePlayer()
    }
    
    private fun setupViews() {
        try {
            playerView = findViewById(R.id.playerView)
            loadingIndicator = findViewById(R.id.loadingIndicator)
            channelNameText = findViewById(R.id.channelNameText)
            
            channelNameText.text = channelName
            loadingIndicator.visibility = View.VISIBLE
            
            Log.d(TAG, "Views setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up views", e)
            Toast.makeText(this, "خطأ في واجهة المشغل", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun initializePlayer() {
        try {
            Log.d(TAG, "Initializing player...")
            
            player = ExoPlayer.Builder(this).build().also { exoPlayer ->
                playerView.player = exoPlayer
                
                val mediaItem = MediaItem.fromUri(channelUrl)
                exoPlayer.setMediaItem(mediaItem)
                
                Log.d(TAG, "MediaItem set, preparing...")
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                Log.d(TAG, "STATE_BUFFERING")
                                loadingIndicator.visibility = View.VISIBLE
                            }
                            Player.STATE_READY -> {
                                Log.d(TAG, "STATE_READY - Playing!")
                                loadingIndicator.visibility = View.GONE
                                Toast.makeText(this@PlayerActivity, "جاري التشغيل...", Toast.LENGTH_SHORT).show()
                            }
                            Player.STATE_ENDED -> {
                                Log.d(TAG, "STATE_ENDED")
                                loadingIndicator.visibility = View.GONE
                            }
                            Player.STATE_IDLE -> {
                                Log.d(TAG, "STATE_IDLE")
                                loadingIndicator.visibility = View.GONE
                            }
                        }
                    }
                    
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        loadingIndicator.visibility = View.GONE
                        Log.e(TAG, "Player error: ${error.errorCodeName}", error)
                        Log.e(TAG, "Error message: ${error.message}")
                        Log.e(TAG, "Error cause: ${error.cause}")
                        
                        Toast.makeText(
                            this@PlayerActivity,
                            "خطأ في التشغيل: ${error.errorCodeName}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
            
            Log.d(TAG, "Player initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing player", e)
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop - releasing player")
        releasePlayer()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy - releasing player")
        releasePlayer()
    }
    
    private fun releasePlayer() {
        player?.release()
        player = null
    }
}
