package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.EnablePaxsenixYouTubeKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.dd3boh.paxsenix.PaxsenixLyrics

object PaxsenixYouTubeLyricsProvider : LyricsProvider {
    override val name = "Paxsenix: YouTube"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnablePaxsenixYouTubeKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = PaxsenixLyrics.getYouTubeLyrics(
        title = LyricsSanitizer.cleanTitle(title),
        artist = LyricsSanitizer.cleanArtist(artist),
        durationSeconds = duration,
    )
}
