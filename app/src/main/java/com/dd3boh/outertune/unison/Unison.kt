package com.dd3boh.outertune.unison

object Unison {
    var logger: ((String) -> Unit)? = null

    suspend fun getLyrics(
        videoId: String,
        title: String,
        artist: String,
        album: String?,
        durationSeconds: Int,
    ): Result<String> {
        logger?.invoke("Unison lyrics requested for $title - $artist")
        return Result.failure(IllegalStateException("Unison provider is currently unavailable"))
    }

    suspend fun getAllLyrics(
        videoId: String,
        title: String,
        artist: String,
        album: String?,
        durationSeconds: Int,
        callback: (String) -> Unit,
    ) {
        getLyrics(videoId, title, artist, album, durationSeconds).onSuccess(callback)
    }
}
