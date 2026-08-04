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
import com.zionhuang.innertube.YouTube

object YouTubeSubtitleLyricsProvider : LyricsProvider {
    override val name = "YouTube Subtitle"
    override fun isEnabled(context: Context) = true

    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String> = runCatching {
        if (id.length != 11 || id.startsWith("local:")) {
            throw IllegalArgumentException("Not a valid YouTube video ID: $id")
        }
        YouTube.transcript(id).getOrThrow()
    }

    override suspend fun getAllLyrics(id: String, title: String, artist: String, duration: Int, callback: (String) -> Unit) {
        YouTube.transcript(id).onSuccess(callback)
    }
}
