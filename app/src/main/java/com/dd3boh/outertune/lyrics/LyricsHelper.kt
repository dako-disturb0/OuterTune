/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.lyrics

import android.content.Context
import android.util.LruCache
import com.dd3boh.outertune.constants.LyricSourcePrefKey
import com.dd3boh.outertune.constants.LyricTrimKey
import com.dd3boh.outertune.constants.LyricsProviderOrderKey
import com.dd3boh.outertune.constants.MultilineLrcKey
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.db.entities.LyricsEntity
import com.dd3boh.outertune.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.dd3boh.outertune.utils.reportException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.akanework.gramophone.logic.utils.LrcUtils
import org.akanework.gramophone.logic.utils.SemanticLyrics
import org.akanework.gramophone.logic.utils.parseLrc
import javax.inject.Inject

class LyricsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    val database: MusicDatabase
) {
    /**
     * Default ordered list of all available lyrics providers.
     * Paxsenix (Apple Music) is first to prioritize word-by-word TTML lyrics.
     */
    val allProviders: List<LyricsProvider> = listOf(
        PaxsenixLyricsProvider,
        BetterLyricsProvider,
        SimpMusicLyricsProvider,
        YouTubeSubtitleLyricsProvider,
        LrcLibLyricsProvider,
        KuGouLyricsProvider,
        YouTubeLyricsProvider,
    )

    private val cache = LruCache<String, List<LyricsResult>>(MAX_CACHE_SIZE)
    private val singleLyricsCache = LruCache<String, String>(MAX_CACHE_SIZE)

    /**
     * Returns the ordered list of providers respecting the user-configured priority.
     * Providers not in the saved order are appended at the end.
     */
    private fun getOrderedProviders(): List<LyricsProvider> {
        val savedOrder = context.dataStore.get(LyricsProviderOrderKey, "")
        if (savedOrder.isBlank()) return allProviders

        val orderList = savedOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val providerMap = allProviders.associateBy { it.name }

        val ordered = orderList.mapNotNull { providerMap[it] }
        val remaining = allProviders.filter { it.name !in orderList }
        return ordered + remaining
    }

    /**
     * Consistent cache key based on title+artist
     */
    private fun lyricsCacheKey(title: String, artists: String): String =
        "$artists-$title".replace(" ", "")

    private val MediaMetadata.lyricsCacheKey: String
        get() = lyricsCacheKey(title, artists.joinToString { it.name })

    /**
     * Retrieve lyrics from all sources
     *
     * How lyrics are resolved are determined by PreferLocalLyrics settings key. If this is true, prioritize local lyric
     * files over all cloud providers, true is vice versa.
     *
     * Lyrics stored in the database are fetched first. If this is not available, it is resolved by other means.
     * If local lyrics are preferred, lyrics from the lrc file is fetched, and then resolve by other means.
     *
     * @param mediaMetadata Song to fetch lyrics for
     * @param forceRefresh If true, bypasses cache and database lookup and re-fetches from providers
     */
    suspend fun getLyrics(mediaMetadata: MediaMetadata, forceRefresh: Boolean = false): SemanticLyrics? {
        val trim = context.dataStore.get(LyricTrimKey, defaultValue = false)
        val multiline = context.dataStore.get(MultilineLrcKey, defaultValue = true)
        val prefLocal = context.dataStore.get(LyricSourcePrefKey, true)
        val cacheKey = mediaMetadata.lyricsCacheKey

        if (forceRefresh) {
            invalidateCache(cacheKey)
        } else {
            // Check single lyrics cache first
            singleLyricsCache.get(cacheKey)?.let { cachedLyrics ->
                if (cachedLyrics == LYRICS_NOT_FOUND) return null
                return parseLrc(cachedLyrics, trim, multiline)
            }

            // Check multi-results cache
            val cached = cache.get(cacheKey)?.firstOrNull()
            if (cached != null) {
                return parseLrc(cached.lyrics, trim, multiline)
            }
        }

        // Check database
        val dbLyrics = database.lyrics(mediaMetadata.id).let { it.first()?.lyrics }
        if (dbLyrics != null && dbLyrics != LYRICS_NOT_FOUND && !prefLocal) {
            singleLyricsCache.put(cacheKey, dbLyrics)
            return parseLrc(dbLyrics, trim, multiline)
        }

        val localLyrics: SemanticLyrics? =
            getLocalLyrics(mediaMetadata, LrcUtils.LrcParserOptions(trim, multiline, "Unable to parse lyrics"))
        val remoteLyrics: String?

        // fallback to secondary provider when primary is unavailable
        if (prefLocal) {
            if (localLyrics != null) {
                return localLyrics
            }
            if (dbLyrics != null && dbLyrics != LYRICS_NOT_FOUND) {
                singleLyricsCache.put(cacheKey, dbLyrics)
                return parseLrc(dbLyrics, trim, multiline)
            }

            // "lazy eval" the remote lyrics cuz it is laughably slow
            remoteLyrics = getRemoteLyrics(mediaMetadata)
            if (remoteLyrics != null) {
                singleLyricsCache.put(cacheKey, remoteLyrics)
                database.query {
                    upsert(
                        LyricsEntity(
                            id = mediaMetadata.id,
                            lyrics = remoteLyrics
                        )
                    )
                }
                return parseLrc(remoteLyrics, trim, multiline)
            }
        } else {
            remoteLyrics = getRemoteLyrics(mediaMetadata)
            if (remoteLyrics != null) {
                singleLyricsCache.put(cacheKey, remoteLyrics)
                database.query {
                    upsert(
                        LyricsEntity(
                            id = mediaMetadata.id,
                            lyrics = remoteLyrics
                        )
                    )
                }
                return parseLrc(remoteLyrics, trim, multiline)
            } else if (localLyrics != null) {
                return localLyrics
            }
        }

        // Mark as not found in cache and database
        singleLyricsCache.put(cacheKey, LYRICS_NOT_FOUND)
        database.query {
            upsert(
                LyricsEntity(
                    id = mediaMetadata.id,
                    lyrics = LYRICS_NOT_FOUND
                )
            )
        }
        return null
    }

    /**
     * Lookup lyrics from remote providers concurrently in user priority order.
     */
    private suspend fun getRemoteLyrics(mediaMetadata: MediaMetadata): String? = withContext(Dispatchers.IO) {
        val cleanTitle = LyricsSanitizer.cleanTitle(mediaMetadata.title)
        val cleanArtist = LyricsSanitizer.cleanArtist(mediaMetadata.artists.joinToString { it.name })
        val enabledProviders = getOrderedProviders().filter { it.isEnabled(context) }
        if (enabledProviders.isEmpty()) return@withContext null

        suspend fun fetchFromProvider(provider: LyricsProvider, title: String): String? {
            return try {
                kotlinx.coroutines.withTimeoutOrNull(3500L) {
                    provider.getLyrics(
                        mediaMetadata.id,
                        title,
                        cleanArtist,
                        mediaMetadata.duration
                    ).getOrNull()?.takeIf { it.isNotBlank() && it != LYRICS_NOT_FOUND }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }

        // Primary pass with cleaned title in parallel
        val cleanJobs = enabledProviders.map { provider ->
            provider to async { fetchFromProvider(provider, cleanTitle) }
        }

        for ((_, job) in cleanJobs) {
            val result = job.await()
            if (result != null) {
                cleanJobs.forEach { it.second.cancel() }
                return@withContext result
            }
        }

        // Secondary fallback pass with raw title if cleanTitle differs
        if (cleanTitle != mediaMetadata.title) {
            val rawJobs = enabledProviders.map { provider ->
                provider to async { fetchFromProvider(provider, mediaMetadata.title) }
            }
            for ((_, job) in rawJobs) {
                val result = job.await()
                if (result != null) {
                    rawJobs.forEach { it.second.cancel() }
                    return@withContext result
                }
            }
        }

        null
    }

    /**
     * Lookup lyrics from local disk (.lrc) file
     */
    private fun getLocalLyrics(
        mediaMetadata: MediaMetadata,
        parserOptions: LrcUtils.LrcParserOptions
    ): SemanticLyrics? {
        if (LocalLyricsProvider.isEnabled(context) && mediaMetadata.localPath != null) {
            return LocalLyricsProvider.getLyricsNew(
                mediaMetadata.localPath,
                parserOptions
            )
        }

        return null
    }

    suspend fun getAllLyrics(
        mediaId: String,
        songTitle: String,
        songArtists: String,
        duration: Int,
        callback: (LyricsResult) -> Unit,
    ) {
        val cleanTitle = LyricsSanitizer.cleanTitle(songTitle)
        val cleanArtist = LyricsSanitizer.cleanArtist(songArtists)
        val cacheKey = lyricsCacheKey(cleanTitle, cleanArtist)
        cache.get(cacheKey)?.let { results ->
            results.forEach {
                callback(it)
            }
            return
        }
        val allResult = mutableListOf<LyricsResult>()
        withContext(Dispatchers.IO) {
            getOrderedProviders().forEach { provider ->
                if (provider.isEnabled(context)) {
                    try {
                        provider.getAllLyrics(mediaId, cleanTitle, cleanArtist, duration) { lyrics ->
                            if (lyrics.isNotBlank() && lyrics != LYRICS_NOT_FOUND) {
                                val result = LyricsResult(provider.name, lyrics)
                                allResult += result
                                callback(result)
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        reportException(e)
                    }
                }
            }
        }
        cache.put(cacheKey, allResult)
    }

    fun clearCache() {
        cache.evictAll()
        singleLyricsCache.evictAll()
    }

    private fun invalidateCache(cacheKey: String) {
        cache.remove(cacheKey)
        singleLyricsCache.remove(cacheKey)
    }

    companion object {
        private const val MAX_CACHE_SIZE = 16

        /**
         * Default provider order (names must match provider.name values exactly)
         */
        val DEFAULT_PROVIDER_ORDER = listOf(
            "Paxsenix (Apple Music)",
            "BetterLyrics",
            "SimpMusic",
            "YouTube Subtitle",
            "LrcLib",
            "Kugou",
            "YouTube Music",
        )
    }
}

data class LyricsResult(
    val providerName: String,
    val lyrics: String,
)
