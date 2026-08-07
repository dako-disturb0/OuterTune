package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.EnablePaxsenixSpotifyKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.dd3boh.paxsenix.PaxsenixLyrics

object PaxsenixSpotifyLyricsProvider : LyricsProvider {
    override val name = "Paxsenix: Spotify"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnablePaxsenixSpotifyKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = PaxsenixLyrics.getSpotifyLyrics(
        title = LyricsSanitizer.cleanTitle(title),
        artist = LyricsSanitizer.cleanArtist(artist),
        durationSeconds = duration,
    )
}
