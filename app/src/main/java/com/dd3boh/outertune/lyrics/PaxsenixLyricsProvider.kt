package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.EnablePaxsenixKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.dd3boh.paxsenix.PaxsenixLyrics

object PaxsenixLyricsProvider : LyricsProvider {
    override val name = "Paxsenix (Auto)"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnablePaxsenixKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = PaxsenixLyrics.getLyrics(
        title = LyricsSanitizer.cleanTitle(title),
        artist = LyricsSanitizer.cleanArtist(artist),
        durationSeconds = duration,
    )

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        PaxsenixLyrics.getAllLyrics(
            title = LyricsSanitizer.cleanTitle(title),
            artist = LyricsSanitizer.cleanArtist(artist),
            duration = duration,
            callback = callback,
        )
    }
}
