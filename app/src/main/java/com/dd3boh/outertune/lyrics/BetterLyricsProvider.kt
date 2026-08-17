package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.betterlyrics.BetterLyrics
import com.dd3boh.outertune.constants.BetterLyricsCustomNodeUrlKey
import com.dd3boh.outertune.constants.BetterLyricsWithNodesKey
import com.dd3boh.outertune.constants.EnableBetterLyricsKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get

object BetterLyricsProvider : LyricsProvider {
    override val name = "BetterLyrics"

    @Volatile
    private var withNodes: Boolean = false

    @Volatile
    private var customNodeUrl: String = "https://betenode.vercel.app"

    override fun isEnabled(context: Context): Boolean {
        withNodes = context.dataStore[BetterLyricsWithNodesKey] ?: false
        customNodeUrl = context.dataStore[BetterLyricsCustomNodeUrlKey] ?: "https://betenode.vercel.app"
        return context.dataStore[EnableBetterLyricsKey] ?: true
    }

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = BetterLyrics.getLyrics(
        title = LyricsSanitizer.cleanTitle(title),
        artist = LyricsSanitizer.cleanArtist(artist),
        durationSeconds = duration,
        customNodeUrl = customNodeUrl,
        withNodes = withNodes
    )

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        BetterLyrics.getAllLyrics(
            title = LyricsSanitizer.cleanTitle(title),
            artist = LyricsSanitizer.cleanArtist(artist),
            durationSeconds = duration,
            customNodeUrl = customNodeUrl,
            withNodes = withNodes,
            callback = callback,
        )
    }
}
