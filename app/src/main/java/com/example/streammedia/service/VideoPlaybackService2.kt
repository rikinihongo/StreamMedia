package com.example.streammedia.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.streammedia.MainActivity

@UnstableApi
class VideoPlaybackService2 : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()

        // Tạo Player
        player = ExoPlayer.Builder(this)
            .build()

        // Tạo MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .build()

        // Tạo Notification cho foreground service
        notificationManager = PlayerNotificationManager.Builder(
            this,
            1001, // notification channel ID (sẽ tạo sau)
            "media3_channel"
        )
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player) = "Video đang phát"
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = Intent(this@VideoPlaybackService2, MainActivity::class.java)
                    return PendingIntent.getActivity(
                        this@VideoPlaybackService2,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }

                override fun getCurrentContentText(player: Player) =
                    player.mediaMetadata.title ?: "Unknown"

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ) = null
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    startForeground(notificationId, notification)
                }

                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    stopSelf()
                }
            })
            .build()

        notificationManager.setPlayer(player)
        notificationManager.setMediaSessionToken(mediaSession!!.platformToken)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Khi người dùng swipe app khỏi recent → dừng phát
        player.stop()
        stopSelf()
    }

    override fun onDestroy() {
        notificationManager.setPlayer(null)
        player.release()
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}