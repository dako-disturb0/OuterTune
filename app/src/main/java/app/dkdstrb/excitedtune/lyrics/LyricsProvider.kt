/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package app.dkdstrb.excitedtune.lyrics

import android.content.Context

interface LyricsProvider {
    val name: String
    fun isEnabled(context: Context): Boolean
    suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String>
    suspend fun getAllLyrics(id: String, title: String, artist: String, duration: Int, callback: (String) -> Unit) {
        getLyrics(id, title, artist, duration).onSuccess(callback)
    }
}