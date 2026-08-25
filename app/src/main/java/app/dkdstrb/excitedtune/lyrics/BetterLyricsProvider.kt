package app.dkdstrb.excitedtune.lyrics

import android.content.Context
import com.dd3boh.betterlyrics.BetterLyrics
import app.dkdstrb.excitedtune.constants.EnableBetterLyricsKey
import app.dkdstrb.excitedtune.utils.dataStore
import app.dkdstrb.excitedtune.utils.get

object BetterLyricsProvider : LyricsProvider {
    override val name = "BetterLyrics"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableBetterLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = BetterLyrics.getLyrics(
        title = LyricsSanitizer.cleanTitle(title),
        artist = LyricsSanitizer.cleanArtist(artist),
        durationSeconds = duration
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
            callback = callback,
        )
    }
}
