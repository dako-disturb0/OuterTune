package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.EnablePaxsenixAppleMusicKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.dd3boh.paxsenix.PaxsenixLyrics

object PaxsenixAppleMusicLyricsProvider : LyricsProvider {
    override val name = "Paxsenix: Apple Music"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnablePaxsenixAppleMusicKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = PaxsenixLyrics.getAppleMusicLyrics(
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
        getLyrics(id, title, artist, duration).onSuccess(callback)
    }
}
