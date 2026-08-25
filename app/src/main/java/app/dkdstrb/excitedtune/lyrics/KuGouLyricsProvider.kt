package app.dkdstrb.excitedtune.lyrics

import android.content.Context
import app.dkdstrb.excitedtune.constants.EnableKugouKey
import app.dkdstrb.excitedtune.utils.dataStore
import app.dkdstrb.excitedtune.utils.get
import com.zionhuang.kugou.KuGou

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableKugouKey] ?: true

    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String> =
        KuGou.getLyrics(
            LyricsSanitizer.cleanTitle(title),
            LyricsSanitizer.cleanArtist(artist),
            duration
        )

    override suspend fun getAllLyrics(id: String, title: String, artist: String, duration: Int, callback: (String) -> Unit) {
        KuGou.getAllPossibleLyricsOptions(
            LyricsSanitizer.cleanTitle(title),
            LyricsSanitizer.cleanArtist(artist),
            duration,
            callback
        )
    }
}
