# New session - 2026-08-25T02:42:25.771Z

**Session ID:** ses_fc933d295ffeOnESiul332fnNv
**Created:** 8/25/2026, 2:42:25 AM
**Updated:** 8/25/2026, 2:46:46 AM

---

## User

hai si misterius

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 33.5s)

_Thinking:_

The user wrote "hai si misterius" — Indonesian, roughly "hi, mysterious one". Just a casual greeting. Respond briefly in Indonesian.

Hai! Ada yang bisa saya bantu di repo OuterTune ini?

---

## User

Perbaiki Masalah Force Close

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 26.9s)

_Thinking:_

The user says "Perbaiki Masalah Force Close" (Fix the Force Close problem) in the OuterTune repo. I need to investigate. Let me look at logs or find crash info. First, let me explore the project structure and check for any crash logs or recent changes.

I should ask for details, but better to investigate first — maybe there's a stack trace somewhere, or git log shows recent changes. Let me look around.

**Tool: bash**

**Input:**
```json
{
  "command": "git log --oneline -10 && git status"
}
```

**Output:**
```
fac84d94 fix(ui): add breathing room between floating miniplayer and navbar
85e2da11 Update devcontainer.json configuration
6b328980 Update Dockerfile for dependencies and tools installation
e879e413 Add GitHub CLI feature and update settings
6f5eb496 Enhance Dockerfile with GitHub CLI and Antigravity setup
5f6c94fd revert: restore repository tree to commit 95ad2f45 (feat(player): improve miniplayer layout)
ed0e09cf fix: eliminate unsafe force-unwraps causing NPE and fix compilation errors
1761145b fix(ci): fix all compilation errors and missing references in release build
d5d5d6e0 ci: install CMake 3.31.6 before building release APKs
a5d839aa style: separate mini player from bottom navigation bar into a floating card
On branch fix/ui-cramped-spacing
Your branch is up to date with 'origin/fix/ui-cramped-spacing'.

nothing to commit, working tree clean

```

**Tool: glob**

**Input:**
```json
{
  "pattern": "*.md"
}
```

**Output:**
```
/home/container/OuterTune/STRUCTURE.md
/home/container/OuterTune/README.md
/home/container/OuterTune/CONTRIBUTING.md
/home/container/OuterTune/TASK3E.md
/home/container/OuterTune/Modules.md
```

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 16.6s)

_Thinking:_

The user wants to fix a force close (crash). I need more info — what's the crash? Let me check if there's a stack trace or logcat available. Maybe there's a device connected, or maybe recent commits hint at the issue. Let me ask for details while investigating. Actually let me check for adb / logcat availability first.

**Tool: bash**

**Input:**
```json
{
  "command": "adb devices 2>/dev/null; ls /tmp/opencode 2>/dev/null"
}
```

**Output:**
```
(no output)
```

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 33.5s)

_Thinking:_

No adb device, no crash log in /tmp/opencode. I need to know what the force close is. Let me check if there are recent changes that could cause crashes, e.g., the "unsafe force-unwraps" commit was reverted? Actually commit ed0e09cf fixed unsafe force-unwraps, then 5f6c94fd reverted to restore tree to 95ad2f45. So maybe the force-unwrap fixes were reverted and crashes remain.

Let me ask the user for details: when does it crash (on launch?), do they have a logcat/stack trace?

Actually let me first investigate a bit — search for suspicious `!!` force unwraps or check what changed. But without a stack trace this is guesswork. Better to ask the user for the scenario and any logs.

**Tool: question**

**Input:**
```json
{
  "questions": [
    {
      "header": "Info Crash",
      "options": [
        {
          "description": "Aplikasi langsung force close saat dibuka",
          "label": "Crash saat aplikasi dibuka"
        },
        {
          "description": "Saya punya file logcat / stack trace yang bisa dibagikan",
          "label": "Ada logcat/stack trace"
        },
        {
          "description": "Crash terjadi saat memutar lagu, navigasi, dll.",
          "label": "Crash saat aksi tertentu"
        },
        {
          "description": "Periksa kode dan cari penyebab crash yang mungkin",
          "label": "Tidak tahu, tolong cari sendiri"
        }
      ],
      "question": "Kapan force close-nya terjadi, dan apakah ada log/stack trace?"
    }
  ]
}
```

**Output:**
```
User has answered your questions: "Kapan force close-nya terjadi, dan apakah ada log/stack trace?"="Crash saat aplikasi dibuka". You can now continue with the user's answers in mind.
```

---

## User

gaada java disini, gausah install

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 8.4s)

_Thinking:_

User says "there's no java here, don't install" — meaning don't try to build/run with Java/Gradle. So I should fix the crash by static analysis only. Crash on app launch. Let me investigate the code for launch-path crashes: Application class, MainActivity, DI modules, etc.

Let me explore the startup path.

Siap, analisa statis saja tanpa build. Saya periksa jalur startup aplikasi (Application, MainActivity, DI).

**Tool: bash**

**Input:**
```json
{
  "command": "ls && find . -name \"MainActivity.kt\" -o -name \"*Application*.kt\" | grep -v build"
}
```

**Output:**
```
CONTRIBUTING.md
LICENSE
Modules.md
README.md
STRUCTURE.md
TASK3E.md
app
assets
betterlyrics
build.gradle.kts
build.sh
fastlane
ffMetadataEx
gradle
gradle.properties
gradlew
gradlew.bat
innertube
kugou
lint.xml
lrclib
material-color-utilities
media
paxsenix
settings.gradle.kts
simpmusic
taglib
./app/src/main/java/com/dd3boh/outertune/MainActivity.kt

```

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 9.7s)

**Tool: bash**

**Input:**
```json
{
  "command": "find app/src/main -name \"*.kt\" -path \"*App*\" | grep -v build; ls app/src/main/java/com/dd3boh/outertune/ | head -30"
}
```

**Output:**
```
app/src/main/java/com/dd3boh/outertune/di/AppModule.kt
app/src/main/java/com/dd3boh/outertune/ui/screens/settings/fragments/AppearanceFrag.kt
app/src/main/java/com/dd3boh/outertune/ui/screens/settings/AppearanceSettings.kt
app/src/main/java/com/dd3boh/outertune/ui/utils/AppBar.kt
app/src/main/java/com/dd3boh/outertune/App.kt
App.kt
MainActivity.kt
MainActivityUtils.kt
constants
db
di
extensions
lyrics
models
playback
ui
utils
viewmodels

```

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/MainActivity.kt",
  "limit": 120
}
```

**Output:**
```
<path>/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/MainActivity.kt</path>
<type>file</type>
<content>
1: /*
2:  * Copyright (C) 2024 z-huang/InnerTune
3:  * Copyright (C) 2025 O​u​t​er​Tu​ne Project
4:  *
5:  * SPDX-License-Identifier: GPL-3.0
6:  *
7:  * For any other attributions, refer to the git commit history
8:  */
9: 
10: package com.dd3boh.outertune
11: 
12: import android.annotation.SuppressLint
13: import android.app.NotificationManager
14: import android.content.Intent
15: import android.os.Build
16: import android.os.Bundle
17: import android.util.Log
18: import android.widget.Toast
19: import androidx.activity.ComponentActivity
20: import androidx.activity.compose.setContent
21: import androidx.activity.result.contract.ActivityResultContracts
22: import androidx.activity.viewModels
23: import androidx.compose.animation.core.Spring
24: import androidx.compose.animation.core.animateDpAsState
25: import androidx.compose.animation.core.spring
26: import androidx.compose.animation.core.tween
27: import androidx.compose.animation.fadeIn
28: import androidx.compose.animation.fadeOut
29: import androidx.compose.animation.scaleIn
30: import androidx.compose.animation.scaleOut
31: import androidx.compose.animation.slideInHorizontally
32: import androidx.compose.animation.slideOutHorizontally
33: import androidx.compose.foundation.Image
34: import androidx.compose.foundation.background
35: import androidx.compose.foundation.interaction.MutableInteractionSource
36: import androidx.compose.foundation.isSystemInDarkTheme
37: import androidx.compose.foundation.layout.Box
38: import androidx.compose.foundation.layout.BoxWithConstraints
39: import androidx.compose.foundation.layout.Column
40: import androidx.compose.foundation.layout.Row
41: import androidx.compose.foundation.layout.Spacer
42: import androidx.compose.foundation.layout.WindowInsets
43: import androidx.compose.foundation.layout.WindowInsetsSides
44: import androidx.compose.foundation.layout.add
45: import androidx.compose.foundation.layout.displayCutout
46: import androidx.compose.foundation.layout.fillMaxHeight
47: import androidx.compose.foundation.layout.fillMaxSize
48: import androidx.compose.foundation.layout.fillMaxWidth
49: import androidx.compose.foundation.layout.height
50: import androidx.compose.foundation.layout.offset
51: import androidx.compose.foundation.layout.only
52: import androidx.compose.foundation.layout.padding
53: import androidx.compose.foundation.layout.safeDrawing
54: import androidx.compose.foundation.layout.size
55: import androidx.compose.foundation.layout.systemBars
56: import androidx.compose.foundation.layout.width
57: import androidx.compose.foundation.layout.windowInsetsPadding
58: import androidx.compose.foundation.rememberScrollState
59: import androidx.compose.foundation.shape.RoundedCornerShape
60: import androidx.compose.foundation.verticalScroll
61: import androidx.compose.material3.ExperimentalMaterial3Api
62: import androidx.compose.material3.Icon
63: import androidx.compose.material3.LocalContentColor
64: import androidx.compose.material3.MaterialTheme
65: import androidx.compose.material3.NavigationBar
66: import androidx.compose.material3.NavigationBarItem
67: import androidx.compose.material3.NavigationRail
68: import androidx.compose.material3.NavigationRailItem
69: import androidx.compose.material3.SnackbarHost
70: import androidx.compose.material3.SnackbarHostState
71: import androidx.compose.material3.Text
72: import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
73: import androidx.compose.material3.contentColorFor
74: import androidx.compose.material3.rememberModalBottomSheetState
75: import androidx.compose.material3.surfaceColorAtElevation
76: import androidx.compose.runtime.Composable
77: import androidx.compose.runtime.CompositionLocalProvider
78: import androidx.compose.runtime.DisposableEffect
79: import androidx.compose.runtime.LaunchedEffect
80: import androidx.compose.runtime.collectAsState
81: import androidx.compose.runtime.compositionLocalOf
82: import androidx.compose.runtime.derivedStateOf
83: import androidx.compose.runtime.getValue
84: import androidx.compose.runtime.mutableStateOf
85: import androidx.compose.runtime.remember
86: import androidx.compose.runtime.rememberCoroutineScope
87: import androidx.compose.runtime.setValue
88: import androidx.compose.runtime.staticCompositionLocalOf
89: import androidx.compose.ui.Alignment
90: import androidx.compose.ui.Modifier
91: import androidx.compose.ui.graphics.Color
92: import androidx.compose.ui.graphics.toArgb
93: import androidx.compose.ui.hapticfeedback.HapticFeedbackType
94: import androidx.compose.ui.input.nestedscroll.nestedScroll
95: import androidx.compose.ui.platform.LocalDensity
96: import androidx.compose.ui.platform.LocalHapticFeedback
97: import androidx.compose.ui.platform.LocalLayoutDirection
98: import androidx.compose.ui.res.painterResource
99: import androidx.compose.ui.res.stringResource
100: import androidx.compose.ui.text.font.FontWeight
101: import androidx.compose.ui.text.style.TextOverflow
102: import androidx.compose.ui.unit.Dp
103: import androidx.compose.ui.unit.IntOffset
104: import androidx.compose.ui.unit.LayoutDirection
105: import androidx.compose.ui.unit.dp
106: import androidx.compose.ui.util.fastForEach
107: import androidx.core.net.toUri
108: import androidx.core.util.Consumer
109: import androidx.core.view.WindowCompat
110: import androidx.navigation.NavDestination.Companion.hierarchy
111: import androidx.navigation.NavType
112: import androidx.navigation.compose.NavHost
113: import androidx.navigation.compose.composable
114: import androidx.navigation.compose.currentBackStackEntryAsState
115: import androidx.navigation.compose.rememberNavController
116: import androidx.navigation.navArgument
117: import androidx.window.core.layout.WindowWidthSizeClass
118: import com.dd3boh.outertune.constants.AppBarHeight
119: import com.dd3boh.outertune.constants.DEFAULT_ENABLED_TABS
120: import com.dd3boh.outertune.constants.DarkMode

(Showing lines 1-120 of 1122. Use offset=121 to continue.)
</content>
```

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 34.9s)

_Thinking:_

The user says crash on app

---

## User

lambat bet

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 20.2s)

_Thinking:_

User says "lambat bet" (so slow). I should speed up. Let me do targeted greps in parallel instead of reading big files slowly. Crash on launch — common causes: Application.onCreate (App.kt), AppModule DI, force unwraps (`!!`), recent commits mentioned "revert: restore repository tree to commit 95ad2f45" which may have reverted the NPE fixes.

Let me run a few quick parallel searches.

Oke, saya percepat dengan pencarian paralel.

**Tool: bash**

**Input:**
```json
{
  "command": "rg -n '!!\\.' app/src/main/java/com/dd3boh/outertune/App.kt app/src/main/java/com/dd3boh/outertune/MainActivity.kt | head -20"
}
```

**Output:**
```
/usr/bin/bash: line 1: rg: command not found

```

**Tool: bash**

**Input:**
```json
{
  "command": "wc -l app/src/main/java/com/dd3boh/outertune/App.kt && rg -n \"onCreate|lateinit|!!\" app/src/main/java/com/dd3boh/outertune/App.kt | head -30"
}
```

**Output:**
```
231 app/src/main/java/com/dd3boh/outertune/App.kt
/usr/bin/bash: line 1: rg: command not found

```

---

## Assistant (Build · Ox Alpha Free (Unlimited) · 15.6s)

**Tool: grep**

**Input:**
```json
{
  "path": "/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune",
  "pattern": "!!\\.|!!\\[|lateinit var"
}
```

**Output:**
```
Found 57 matches
/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/MainActivity.kt:
  Line 210:     lateinit var database: MusicDatabase

  Line 213:     lateinit var downloadUtil: DownloadUtil

  Line 216:     lateinit var syncUtils: SyncUtils

  Line 218:     lateinit var activityLauncher: ActivityLauncherHelper

  Line 219:     lateinit var connectivityObserver: NetworkConnectivityObserver


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/utils/potoken/PoTokenGenerator.kt:
  Line 53:                     forceRecreate || webPoTokenGenerator == null || webPoTokenGenerator!!.isExpired || webPoTokenSessionId != sessionId

  Line 67:                     webPoTokenStreamingPot = webPoTokenGenerator!!.generatePoToken(webPoTokenSessionId!!)


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/utils/potoken/PoTokenWebView.kt:
  Line 44:     private lateinit var expirationInstant: Instant

  Line 286:                     response.body!!.string()


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/utils/CoilBitmapLoader.kt:
  Line 84:                 result.image!!.toBitmap()

  Line 98:                     BitmapFactory.decodeByteArray(art, 0, art!!.size)


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/utils/scanners/LocalMediaScanner.kt:
  Line 348:                             insert(SongArtistMap(songToUpdate.id, item.first!!.id, index))

  Line 359:                             insert(SongGenreMap(songToUpdate.id, item.first!!.id, index))

  Line 371:                                 album.first!!.copy(

  Line 373:                                     songCount = album.first!!.songCount + 1

  Line 376:                             insert(SongAlbumMap(songToUpdate.id, album.first!!.id, album.first!!.songCount))


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/player/Queue.kt:
  Line 440:                 addAll(detachedQueue!!.getCurrentQueueShuffled())

  Line 971:                                 queueWindows.sumOf { it.mediaItem.metadata!!.duration }


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/player/Player.kt:
  Line 770:                                 navController.navigate("album/${mediaMetadata?.album!!.id}")


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/player/PlayerBackground.kt:
  Line 228:                         listOf(dominantColor!!, dominantColor!!.copy(alpha = 0.6f), dominantColor!!)


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/screens/playlist/LocalPlaylistScreen.kt:
  Line 276:                     text = stringResource(R.string.remove_download_playlist_confirm, playlistWithSongs.first?.playlist!!.name),

  Line 322:                     text = stringResource(R.string.delete_playlist_confirm, playlistWithSongs.first?.playlist!!.name),

  Line 550:                                     title =  playlistWithSongs.first!!.playlist.name,


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/screens/playlist/OnlinePlaylistScreen.kt:
  Line 399:                                                                 update(dbPlaylist!!.playlist.toggleLike())

  Line 455:                                                                         dbPlaylist!!.id

  Line 523:                                                         playlistId = playlist.playEndpoint!!.playlistId,

  Line 547:                                                         playlistId = playlist.playEndpoint!!.playlistId,


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/screens/AlbumScreen.kt:
  Line 426:                 items = albumWithSongs!!.songs,


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/screens/HomeScreen.kt:
  Line 614:                                 is Song -> navController.navigate("album/${it.title.album!!.id}")


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/dialog/AddToPlaylistDialog.kt:
  Line 249:                                 songIds!!.filter {


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/menu/PlayerMenu.kt:
  Line 458:         if (librarySong?.song?.inLibrary != null && !librarySong!!.song.isLocal) {


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/ui/menu/YouTubePlaylistMenu.kt:
  Line 147:                                 update(dbPlaylist!!.playlist.toggleLike())

  Line 179:                         playlistId = playlist.playEndpoint!!.playlistId,

  Line 196:                         playlistId = playlist.playEndpoint!!.playlistId,


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/playback/MediaControllerViewModel.kt:
  Line 60:                                 if (e.cause!!.message != "Session rejected the connection request.")

  Line 96:             ds.callback(instance, controllerLifecycle!!.lifecycle)

  Line 115:             return controllerFuture!!.get()


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/playback/downloadManager/DirectoryMangerOt.kt:
  Line 31:             if (mainDir == null || !mainDir!!.isDirectory) {

  Line 36: //            if (!mainDir!!.listFiles().any { it.name == ".nomedia" }) {

  Line 52:             } else if (!mainDir!!.isDirectory) {


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/playback/ExoDownloadService.kt:
  Line 30:     lateinit var downloadUtil: DownloadUtil


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/playback/MusicService.kt:
  Line 164:     lateinit var database: MusicDatabase

  Line 170:     lateinit var downloadUtil: DownloadUtil

  Line 173:     lateinit var lyricsHelper: LyricsHelper

  Line 176:     lateinit var mediaLibrarySessionCallback: MediaLibrarySessionCallback

  Line 179:     private lateinit var connectivityManager: ConnectivityManager

  Line 187:     lateinit var playerCache: SimpleCache

  Line 191:     lateinit var downloadCache: SimpleCache

  Line 193:     lateinit var player: ExoPlayer

  Line 194:     private lateinit var mediaSession: MediaLibrarySession

  Line 198:     lateinit var syncUtils: SyncUtils

  Line 200:     lateinit var connectivityObserver: NetworkConnectivityObserver

  Line 204:     lateinit var sleepTimer: SleepTimer


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/playback/MediaLibrarySessionCallback.kt:
  Line 54:     lateinit var service: MusicService

  Line 121:                listOf(q.getCurrentSong()!!.toMediaItem()),


/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/App.kt:
  Line 98:                     dataStore[ProxyUrlKey]!!.toInetSocketAddress()

  Line 216:         lateinit var instance: App

```

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/App.kt"
}
```

**Output:**
```
<path>/home/container/OuterTune/app/src/main/java/com/dd3boh/outertune/App.kt</path>
<type>file</type>
<content>
1: /*
2:  * Copyright (C) 2024 z-huang/InnerTune
3:  * Copyright (C) 2025 OuterTune Project
4:  *
5:  * SPDX-License-Identifier: GPL-3.0
6:  *
7:  * For any other attributions, refer to the git commit history
8:  */
9: 
10: package com.dd3boh.outertune
11: 
12: import android.app.Application
13: import android.content.Context
14: import android.util.Log
15: import android.widget.Toast
16: import android.widget.Toast.LENGTH_SHORT
17: import androidx.datastore.preferences.core.edit
18: import coil3.ImageLoader
19: import coil3.PlatformContext
20: import coil3.SingletonImageLoader
21: import coil3.disk.DiskCache
22: import coil3.disk.directory
23: import coil3.memory.MemoryCache
24: import coil3.request.CachePolicy
25: import coil3.request.allowHardware
26: import coil3.request.crossfade
27: import com.dd3boh.outertune.constants.AccountChannelHandleKey
28: import com.dd3boh.outertune.constants.AccountEmailKey
29: import com.dd3boh.outertune.constants.AccountNameKey
30: import com.dd3boh.outertune.constants.ContentCountryKey
31: import com.dd3boh.outertune.constants.ContentLanguageKey
32: import com.dd3boh.outertune.constants.CountryCodeToName
33: import com.dd3boh.outertune.constants.DataSyncIdKey
34: import com.dd3boh.outertune.constants.InnerTubeCookieKey
35: import com.dd3boh.outertune.constants.LanguageCodeToName
36: import com.dd3boh.outertune.constants.MaxImageCacheSizeKey
37: import com.dd3boh.outertune.constants.ProxyEnabledKey
38: import com.dd3boh.outertune.constants.ProxyTypeKey
39: import com.dd3boh.outertune.constants.ProxyUrlKey
40: import com.dd3boh.outertune.constants.SYSTEM_DEFAULT
41: import com.dd3boh.outertune.constants.UseLoginForBrowse
42: import com.dd3boh.outertune.constants.VisitorDataKey
43: import com.dd3boh.outertune.extensions.toEnum
44: import com.dd3boh.outertune.extensions.toInetSocketAddress
45: import com.dd3boh.outertune.utils.CoilBitmapLoader
46: import com.dd3boh.outertune.utils.LocalArtworkPathKeyer
47: import com.dd3boh.outertune.utils.dataStore
48: import com.dd3boh.outertune.utils.get
49: import com.dd3boh.outertune.utils.reportException
50: import com.zionhuang.innertube.YouTube
51: import com.zionhuang.innertube.models.YouTubeLocale
52: import com.zionhuang.kugou.KuGou
53: import dagger.hilt.android.HiltAndroidApp
54: import kotlinx.coroutines.DelicateCoroutinesApi
55: import kotlinx.coroutines.Dispatchers
56: import kotlinx.coroutines.GlobalScope
57: import kotlinx.coroutines.flow.distinctUntilChanged
58: import kotlinx.coroutines.flow.map
59: import kotlinx.coroutines.launch
60: import kotlinx.coroutines.runBlocking
61: import kotlinx.coroutines.withContext
62: import java.net.Proxy
63: import java.util.Locale
64: 
65: @HiltAndroidApp
66: class App : Application(), SingletonImageLoader.Factory {
67:     private val TAG = App::class.simpleName.toString()
68: 
69:     @OptIn(DelicateCoroutinesApi::class)
70:     override fun onCreate() {
71:         super.onCreate()
72: 
73:         if (BuildConfig.DEBUG) {
74:             System.setProperty("kotlinx.coroutines.debug", "on")
75:         }
76: 
77:         instance = this;
78: 
79:         val locale = Locale.getDefault()
80:         val languageTag = locale.toLanguageTag().replace("-Hant", "") // replace zh-Hant-* to zh-*
81:         YouTube.locale = YouTubeLocale(
82:             gl = dataStore[ContentCountryKey]?.takeIf { it != SYSTEM_DEFAULT }
83:                 ?: locale.country.takeIf { it in CountryCodeToName }
84:                 ?: "US",
85:             hl = dataStore[ContentLanguageKey]?.takeIf { it != SYSTEM_DEFAULT }
86:                 ?: locale.language.takeIf { it in LanguageCodeToName }
87:                 ?: languageTag.takeIf { it in LanguageCodeToName }
88:                 ?: "en"
89:         )
90:         if (languageTag == "zh-TW") {
91:             KuGou.useTraditionalChinese = true
92:         }
93: 
94:         if (dataStore[ProxyEnabledKey] == true) {
95:             try {
96:                 YouTube.proxy = Proxy(
97:                     dataStore[ProxyTypeKey].toEnum(defaultValue = Proxy.Type.HTTP),
98:                     dataStore[ProxyUrlKey]!!.toInetSocketAddress()
99:                 )
100:             } catch (e: Exception) {
101:                 Toast.makeText(this, "Failed to parse proxy url.", LENGTH_SHORT).show()
102:                 reportException(e)
103:             }
104:         }
105: 
106:         try {
107:             com.dd3boh.outertune.utils.DnsHelper.applyDns(this)
108:         } catch (e: Exception) {
109:             Log.e(TAG, "Failed to apply DNS: ${e.message}")
110:         }
111: 
112:         if (dataStore[UseLoginForBrowse] != false) {
113:             YouTube.useLoginForBrowse = true
114:         }
115: 
116:         GlobalScope.launch {
117:             dataStore.data
118:                 .map { it[VisitorDataKey] }
119:                 .distinctUntilChanged()
120:                 .collect { visitorData ->
121:                     YouTube.visitorData = visitorData
122:                         ?.takeIf { it != "null" } // Previously visitorData was sometimes saved as "null" due to a bug
123:                         ?: YouTube.visitorData().onFailure {
124:                             withContext(Dispatchers.Main) {
125:                                 Toast.makeText(this@App, "Failed to get visitorData.", LENGTH_SHORT).show()
126:                             }
127:                             reportException(it)
128:                         }.getOrNull()?.also { newVisitorData ->
129:                             dataStore.edit { settings ->
130:                                 settings[VisitorDataKey] = newVisitorData
131:                             }
132:                         }
133:                 }
134:         }
135:         GlobalScope.launch {
136:             dataStore.data
137:                 .map { it[DataSyncIdKey] }
138:                 .distinctUntilChanged()
139:                 .collect { dataSyncId ->
140:                     YouTube.dataSyncId = dataSyncId?.let {
141:                         /*
142:                          * Workaround to avoid breaking older installations that have a dataSyncId
143:                          * that contains "||" in it.
144:                          * If the dataSyncId ends with "||" and contains only one id, then keep the
145:                          * id before the "||".
146:                          * If the dataSyncId contains "||" and is not at the end, then keep the
147:                          * second id.
148:                          * This is needed to keep using the same account as before.
149:                          */
150:                         it.takeIf { !it.contains("||") }
151:                             ?: it.takeIf { it.endsWith("||") }?.substringBefore("||")
152:                             ?: it.substringAfter("||")
153:                     }
154:                 }
155:         }
156:         GlobalScope.launch {
157:             dataStore.data
158:                 .map { it[InnerTubeCookieKey] }
159:                 .distinctUntilChanged()
160:                 .collect { cookie ->
161:                     try {
162:                         YouTube.cookie = cookie
163:                     } catch (e: Exception) {
164:                         // we now allow user input now, here be the demons. This serves as a last ditch effort to avoid a crash loop
165:                         Log.e(TAG, "Could not parse cookie. Clearing existing cookie. ${e.message}")
166:                         forgetAccount(this@App)
167:                     }
168:                 }
169:         }
170:     }
171: 
172:     override fun newImageLoader(context: PlatformContext): ImageLoader {
173:         val cacheSize = dataStore[MaxImageCacheSizeKey]
174: 
175:         // will crash app if you set to 0 after cache starts being used
176:         if (cacheSize == 0) {
177:             return ImageLoader.Builder(this)
178:                 .components {
179:                     add(CoilBitmapLoader.Factory(this@App))
180:                     add(LocalArtworkPathKeyer())
181:                 }
182:                 .crossfade(true)
183:                 .allowHardware(false)
184:                 .memoryCache {
185:                     MemoryCache.Builder()
186:                         .maxSizePercent(context, 0.3)
187:                         .build()
188:                 }
189:                 .diskCachePolicy(CachePolicy.DISABLED)
190:                 .build()
191:         }
192: 
193:         return ImageLoader.Builder(this)
194:             .components {
195:                 add(CoilBitmapLoader.Factory(this@App))
196:                 add(LocalArtworkPathKeyer())
197:             }
198:             .crossfade(true)
199:             .allowHardware(false)
200:             .memoryCache {
201:                 MemoryCache.Builder()
202:                     .maxSizePercent(context, 0.3)
203:                     .build()
204:             }
205:             .diskCache(
206:                 // Local images should bypass with DataSource.DISK
207:                 DiskCache.Builder()
208:                     .directory(cacheDir.resolve("coil"))
209:                     .maxSizeBytes((cacheSize ?: 512) * 1024 * 1024L)
210:                     .build()
211:             )
212:             .build()
213:     }
214: 
215:     companion object {
216:         lateinit var instance: App
217:             private set
218: 
219:         fun forgetAccount(context: Context) {
220:             runBlocking {
221:                 context.dataStore.edit { settings ->
222:                     settings.remove(InnerTubeCookieKey)
223:                     settings.remove(VisitorDataKey)
224:                     settings.remove(DataSyncIdKey)
225:                     settings.remove(AccountNameKey)
226:                     settings.remove(AccountEmailKey)
227:                     settings.remove(AccountChannelHandleKey)
228:                 }
229:             }
230:         }
231:     }
232: }

(End of file - total 232 lines)
</content>
```

---

## Assistant (Build · Ox Alpha Free (Unlimited))

---

