package com.example.streammedia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PictureInPictureParams
import android.content.ComponentName
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.MoreExecutors
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.TrackSelectionDialogBuilder

@UnstableApi
class MainActivity : AppCompatActivity() {
    private var playerView: PlayerView? = null
    private var controller: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //createNotificationChannel()
        initializePlayer()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "media3_channel",
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo phát media nền"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun initializePlayer() {
        playerView = findViewById(R.id.player_view)
        val btnTrack = findViewById<Button>(R.id.btnShowTrack)

        // Kết nối đến service
        val sessionToken = SessionToken(this, ComponentName(this, VideoPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            controller = controllerFuture.get()
            playerView?.player = controller

            playerView?.useController = true
            playerView?.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)

            // Ví dụ phát một video MP4 hoặc HLS
            val mediaItem2 =
                MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            // Hoặc HLS: "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8"
            val mediaItem3 =
                MediaItem.fromUri("https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8")

            val mediaItem = mediaItem3 //createMediaItem(sampleVideos.first())

            controller?.setMediaItem(mediaItem)
            controller?.prepare()
            controller?.play()

            // Tự động vào PiP khi minimize (Android 8+)
            playerView?.setControllerVisibilityListener(
                PlayerView.ControllerVisibilityListener { visibility ->
                    if (visibility == View.GONE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                    }
                }
            )
        }, MoreExecutors.directExecutor())

        val cur = controller?.currentPosition
        val dur = controller?.duration

        btnTrack.setOnClickListener {
            showQualitySelectorDialog()
        }
    }

    private fun createMediaItem(videoItem: VideoItem): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(videoItem.title)
            .setDisplayTitle(videoItem.title)
            .setDescription(videoItem.description)
            .setArtist(videoItem.author)
            .setArtworkUri(videoItem.thumbnailUrl.toUri())
            .build()

        return MediaItem.Builder()
            .setUri(videoItem.videoUrl)
            .setMediaId(videoItem.id)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun showQualitySelectorDialog() {
        val controller = controller ?: return

        val dialog = TrackSelectionDialogBuilder(
            this,
            "Chọn chất lượng video",
            controller,  // ← Tự lấy current tracks + current overrides → checked đúng
            C.TRACK_TYPE_VIDEO
        )
            .setTrackNameProvider { format ->
                val heightLabel = when (format.height) {
                    in 2160..Int.MAX_VALUE -> "4K"
                    in 1440..2159 -> "1440p"
                    in 1080..1439 -> "1080p"
                    in 720..1079  -> "720p"
                    in 480..719   -> "480p"
                    in 360..479   -> "360p"
                    else          -> "${format.height}p"
                }
                if (format.bitrate > 0) {
                    val bitrateMbps = format.bitrate / 1_000_000f
                    "$heightLabel (${"%.1f".format(bitrateMbps)} Mbps)"
                } else {
                    heightLabel
                }
            }
            .setShowDisableOption(false)
            .build()

        dialog.show()
    }

    override fun onStop() {
        super.onStop()
       // playerView?.player = null // detach để service tiếp tục chạy nền
    }

    override fun onDestroy() {
        controller?.release()
        super.onDestroy()
    }
}

