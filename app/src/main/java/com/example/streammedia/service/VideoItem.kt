package com.example.streammedia.service


/**
 * Đối tượng đại diện cho một video trong ứng dụng
 */
data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val author: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val duration: Long = 0,
    val isOffline: Boolean = false
) {
    /**
     * Kiểm tra xem video có phải là HLS (m3u8) hay không
     */
    fun isHls(): Boolean = videoUrl.endsWith(".m3u8", ignoreCase = true)

    /**
     * Kiểm tra xem video có phải là MP4 hay không
     */
    fun isMp4(): Boolean = videoUrl.endsWith(".mp4", ignoreCase = true)

    /**
     * Trả về định dạng video dưới dạng chuỗi
     */
    fun getVideoFormat(): String = when {
        isHls() -> "HLS"
        isMp4() -> "MP4"
        else -> "Unknown"
    }
}

val sampleVideos = listOf(
    VideoItem(
        id = "1",
        title = "Big Buck Bunny",
        description = "Big Buck Bunny movie from the Blender Foundation",
        author = "Son Tung M-TP",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        thumbnailUrl = "https://peach.blender.org/wp-content/uploads/bbb-splash.png",
        duration = 596000
    ),
    VideoItem(
        id = "2",
        title = "Elephant Dream",
        description = "The first Blender Foundation movie",
        author = "Blender Foundation",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Elephants_Dream_poster.jpg/1200px-Elephants_Dream_poster.jpg",
        duration = 653000
    ),
    VideoItem(
        id = "3",
        title = "Sintel",
        description = "Third Blender Foundation movie",
        author = "Blender Foundation",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        thumbnailUrl = "https://durian.blender.org/wp-content/uploads/2010/06/09.jpg",
        duration = 888000
    ),
    VideoItem(
        id = "4",
        title = "Tears of Steel",
        description = "Mango project from the Blender Foundation",
        author = "Blender Foundation",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        thumbnailUrl = "https://mango.blender.org/wp-content/uploads/2012/09/01_thom_celia_bridge.jpg",
        duration = 734000
    ),
    VideoItem(
        id = "5",
        title = "HLS Stream Example",
        description = "Streaming example with HLS format",
        author = "Test",
        videoUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
        thumbnailUrl = "https://durian.blender.org/wp-content/uploads/2010/06/09.jpg",
        duration = 888000
    )
)