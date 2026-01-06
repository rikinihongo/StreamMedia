package com.example.streammedia.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.streammedia.MainActivity

@OptIn(UnstableApi::class)
class VideoPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(10_000)          // Nút "lùi 10 giây" (double tap trái)
            .setSeekForwardIncrementMs(10_000)       // Nút "tiến 10 giây" (double tap phải)
            .setPauseAtEndOfMediaItems(true)         // Tự động pause khi video kết thúc (tránh loop không mong muốn)
            .setHandleAudioBecomingNoisy(true)       // Tự động pause khi rút tai nghe hoặc ngắt Bluetooth
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)  // Tối ưu âm thanh cho video
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true  // handleAudioFocus = true → tự động pause khi có cuộc gọi, app khác phát âm thanh
            )
            .setWakeMode(C.WAKE_MODE_NETWORK)        // Giữ CPU/wifi bật khi phát video dài (rất quan trọng cho background)
            .build()

        player.addListener(simpleListener) // chỉ xử lý lỗi và state cơ bản

        // PendingIntent quay về MainActivity khi click notification
        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)  // ← Quan trọng nhất!
            .build()

        // Tự động start foreground + show notification khi player.play()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Swipe app khỏi recent → dừng phát
        player.stop()
        stopSelf()
    }

    override fun onDestroy() {
        player.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private val simpleListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            Log.e("PlaybackService", "Error: ${error.message}", error)
            // Có thể stop service nếu lỗi nghiêm trọng không phục hồi
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                // Tùy chọn: chuyển video tiếp hoặc stop foreground
            }
        }
    }

    private val playerListener =  object : Player.Listener {

        // 1. Thay đổi trạng thái phát (play/pause/buffering/ended)
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {} // Chưa sẵn sàng
                Player.STATE_BUFFERING -> {
                    // Có thể show loading ở notification nếu custom
                }
                Player.STATE_READY -> {
                    // Player sẵn sàng → ẩn loading
                }
                Player.STATE_ENDED -> {
                    // Video kết thúc → có thể chuyển item tiếp theo hoặc pause
                    // Không cần làm gì nếu đã setPauseAtEndOfMediaItems(true)
                }
            }
        }

        // 2. Lỗi phát sinh → xử lý nghiêm trọng
        override fun onPlayerError(error: PlaybackException) {
            when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                    // Không có mạng → có thể thông báo user khi quay lại app
                }
                PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                    // Live stream bị trễ → seek lại current
                    player.seekToDefaultPosition()
                }
                // Các lỗi khác...
            }
            // Log lỗi hoặc gửi analytics
            Log.e("VideoPlayback", "Player error: ${error.message}", error)
        }

        // 3. Thay đổi track (quality, subtitle, audio)
        override fun onTracksChanged(tracks: Tracks) {
            // Cập nhật UI chất lượng hiện tại (nếu bạn có menu quality)
            // Hoặc lưu preference chất lượng user chọn
        }

        // 4. Video size thay đổi (khi bắt đầu phát hoặc đổi quality)
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            // Có thể điều chỉnh aspect ratio của PlayerView từ đây (nếu cần)
        }

        // 5. Metadata thay đổi (title, artwork)
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            // Media3 tự động cập nhật notification từ metadata này
            // Bạn chỉ cần setMediaMetadata khi addMediaItem
        }

        // 6. (Tùy chọn) Khi vị trí phát thay đổi – dùng cho seekbar update
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                // User seek → có thể lưu vị trí xem dở
            }
        }
    }
}