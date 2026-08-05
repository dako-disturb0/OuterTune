package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.EnablePaxsenixNeteaseKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.dd3boh.paxsenix.PaxsenixLyrics

object PaxsenixNeteaseLyricsProvider : LyricsProvider {
    override val name = "Paxsenix: NetEase"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnablePaxsenixNeteaseKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = PaxsenixLyrics.getNeteaseLyrics(
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
