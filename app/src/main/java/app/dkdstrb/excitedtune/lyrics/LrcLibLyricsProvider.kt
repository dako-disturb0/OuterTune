package app.dkdstrb.excitedtune.lyrics

import android.content.Context
import com.dd3boh.lrclib.LrcLib
import app.dkdstrb.excitedtune.constants.EnableLrcLibKey
import app.dkdstrb.excitedtune.utils.dataStore
import app.dkdstrb.excitedtune.utils.get

/**
 * Source: https://github.com/Malopieds/InnerTune
 */
object LrcLibLyricsProvider : LyricsProvider {
    override val name = "LrcLib"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableLrcLibKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = LrcLib.getLyrics(
        LyricsSanitizer.cleanTitle(title),
        LyricsSanitizer.cleanArtist(artist),
        duration
    )

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        LrcLib.getAllLyrics(
            LyricsSanitizer.cleanTitle(title),
            LyricsSanitizer.cleanArtist(artist),
            duration,
            null,
            callback
        )
    }
}
