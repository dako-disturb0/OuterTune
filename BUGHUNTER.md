# 🐛 OuterTune - Bug Hunter & Crash Report

Dokumen ini mencatat seluruh **49 bug kritis** yang ditemukan di codebase OuterTune yang berpotensi menyebabkan **Force Close (Crash)**, **NullPointerException (NPE)**, **Application Not Responding (ANR)**, atau **Resource/Memory Leak**.

---

## 📊 Summary Ringkasan Temuan

| No | Modul | Jumlah Bug | Jenis Main Risk | Status |
|---|---|---|---|---|
| 1 | **MainActivity & App** | 7 | Network Observer Leak, UI Thread Violation, NaN Layout, Unhandled Coroutine Exception | ✅ Fixed |
| 2 | **Playback Module** | 17 | Unsafe `!!` Unwrapping, SQLite Cursor Leak, Non-Thread-Safe PriorityQueue, ExoPlayer Invocation | ✅ Fixed |
| 3 | **ViewModels & DB** | 10 | Singleton DB Closed, `runBlocking` Thread Freeze (ANR), SQL Syntax Error, Infinite Loop | ✅ Fixed |
| 4 | **UI, Models & Utils** | 15 | Unsafe ClassCast (`ArrayList`), Native Retriever Leak, Missing Null Checks, Unsafe Navigation | ✅ Fixed |

---

## 🚨 Daftar Lengkap Bug & Temuan

### 📱 Modul 1: MainActivity & App

#### Bug 1.1: Unhandled `IllegalArgumentException` & System Callback Leak di `NetworkConnectivityObserver`
- **File**: `app/src/main/java/com/dd3boh/outertune/MainActivity.kt:319-324`
- **Deskripsi**: `connectivityObserver` dibuat langsung di body `@Composable` `setContent` tanpa `DisposableEffect`.
- **Penyebab Crash**: Recomposition berulang mendaftarkan callback sistem tanpa melepas yang lama hingga OS melemparkan crash `Too many requests` (>100 callbacks), atau `unregister()` melempar `IllegalArgumentException`.
- **Perbaikan**: Bungkus instansiasi dan pembersihan `connectivityObserver` di dalam `DisposableEffect(Unit)`.

#### Bug 1.2: `IllegalStateException` / `ClassCastException` di `scanInit` (Background Thread)
- **File**: `app/src/main/java/com/dd3boh/outertune/MainActivityUtils.kt:232`
- **Deskripsi**: `scanInit` dipanggil dari background dispatcher (`Dispatchers.IO`) lalu mengeksekusi `(context as MainActivity).permissionLauncher.launch()`.
- **Penyebab Crash**: Android `ActivityResultLauncher.launch()` wajib dipanggil dari UI Thread. Memanggilnya dari thread background melempar `IllegalStateException`. Jika `context` bukan `MainActivity`, memicu `ClassCastException`.
- **Perbaikan**: Pindahkan eksekusi ke `Dispatchers.Main` menggunakan `withContext(Dispatchers.Main)` dan gunakan safe cast `(context as? MainActivity)`.

#### Bug 1.3: `IllegalArgumentException` (NaN pada `.roundToPx()`) saat Layout Measurement
- **File**: `app/src/main/java/com/dd3boh/outertune/MainActivity.kt:802 & 908`
- **Deskripsi**: `playerBottomSheetState.progress` dikalkulasi ke pixel via `.roundToPx()`.
- **Penyebab Crash**: Saat measurement pass awal, `progress` bernilai `Float.NaN`. Memanggil `.roundToPx()` pada `NaN` melemparkan `IllegalArgumentException: Cannot round NaN value.` di Compose.
- **Perbaikan**: Tambahkan guard `if (rawOffset.value.isNaN()) 0.dp else rawOffset`.

#### Bug 1.4: Unhandled Network Exception di `GlobalScope.launch`
- **File**: `app/src/main/java/com/dd3boh/outertune/App.kt:116`
- **Deskripsi**: `YouTube.visitorData()` dipanggil di dalam `GlobalScope.launch` tanpa `try-catch`.
- **Penyebab Crash**: Jika network error (misal `UnknownHostException` / timeout), exception tidak ditangkap dan memicu uncaught exception crash di level aplikasi.
- **Perbaikan**: Gunakan `CoroutineExceptionHandler` atau bungkus dengan `try-catch`.

#### Bug 1.5: `IllegalArgumentException` pada `navController.navigate()` akibat Deep Link ID Unencoded
- **File**: `app/src/main/java/com/dd3boh/outertune/MainActivityUtils.kt:70, 77, 82`
- **Deskripsi**: Parameter deep link seperti `browseId` disisipkan langsung ke route string `album/$browseId`.
- **Penyebab Crash**: Jika ID mengandung karakter khusus/slash (`/`), Navigation Compose gagal mencocokkan route dan melempar `IllegalArgumentException`.
- **Perbaikan**: Bungkus parameter dinamis dengan `Uri.encode(browseId)`.

#### Bug 1.6: `IllegalStateException` Akses `navController.graph` Sebelum Initialized
- **File**: `app/src/main/java/com/dd3boh/outertune/MainActivity.kt:863 & 960`
- **Deskripsi**: `navController.graph.startDestinationId` diakses langsung di click listener `NavigationBar`.
- **Penyebab Crash**: Jika pengguna menekan tombol sebelum NavGraph terpasang, melempar `IllegalStateException: You must call setGraph() before calling getGraph()`.
- **Perbaikan**: Tambahkan safe check / try-catch sebelum mengakses `navController.graph`.

#### Bug 1.7: Blocking Main Thread via `runBlocking` di `forgetAccount()`
- **File**: `app/src/main/java/com/dd3boh/outertune/App.kt:219`
- **Deskripsi**: `forgetAccount()` menggunakan `runBlocking { dataStore.edit { ... } }`.
- **Penyebab Crash**: Menghambat UI Main Thread untuk I/O disk sinkron yang dapat memicu ANR (Application Not Responding).
- **Perbaikan**: Ubah `forgetAccount` menjadi `suspend fun` atau jalankan secara asinkron.

---

### 🎵 Modul 2: Playback Module

#### Bug 2.1: `IndexOutOfBoundsException` & NPE di `DownloadUtil.kt` (Media Format Parsing)
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/DownloadUtil.kt:120-123`
- **Deskripsi**: `mimeType.split("codecs=")[1]` dan `format.contentLength!!`.
- **Penyebab Crash**: Jika `mimeType` tidak memiliki `"codecs="`, `split` menghasilkan list ukuran 1, index `[1]` melempar `IndexOutOfBoundsException`. Jika `contentLength` null, `!!` melempar NPE.
- **Perbaikan**: Gunakan `getOrNull(1)` dan safe fallback `format.contentLength ?: 0L`.

#### Bug 2.2: NullPointerException pada Cached File Access (`span.file!!`)
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/DownloadUtil.kt:235`
- **Deskripsi**: `FileInputStream(file)` di mana `file = span.file` (nullable).
- **Penyebab Crash**: Jika cache span tidak memiliki file lokal (`span.file == null`), melemparkan `NullPointerException`.
- **Perbaikan**: Tambahkan check `val file: File = span.file ?: continue`.

#### Bug 2.3: NullPointerException saat Rescan Downloads
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/DownloadUtil.kt:359`
- **Deskripsi**: `result[s.song.id] = s.song.dateDownload!!`.
- **Penyebab Crash**: `dateDownload` bisa null untuk lagu yang masih dalam antrean download. Unwrapping `!!` melempar NPE.
- **Perbaikan**: Gunakan `s.song.dateDownload?.let { ... }`.

#### Bug 2.4: SQLite Cursor Leak pada Download Index
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/DownloadUtil.kt:285`
- **Deskripsi**: `downloadManager.downloadIndex.getDownloads()` diiterasi tanpa pernah ditutup.
- **Penyebab Crash**: Kebocoran SQLite database cursor memicu `CursorWindowAllocationException`.
- **Perbaikan**: Bungkus penggunaan cursor dengan `use { cursor -> ... }`.

#### Bug 2.5: `IllegalArgumentException` di `ExoDownloadService.kt` (Notification Recovery)
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/ExoDownloadService.kt:53`
- **Deskripsi**: `Notification.Builder.recoverBuilder(this, notification)`.
- **Penyebab Crash**: Memanggil framework `recoverBuilder` pada AndroidX `NotificationCompat` melempar `IllegalArgumentException: Invalid notification`.
- **Perbaikan**: Gunakan `NotificationCompat.Builder` secara konsisten.

#### Bug 2.6: Unhandled `IllegalStateException` pada `MediaControllerViewModel` `onStop`
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/MediaControllerViewModel.kt:136`
- **Deskripsi**: `throw IllegalStateException("controllerFuture?.isCancelled != false")`.
- **Penyebab Crash**: Secara eksplisit melempar unhandled exception saat lifecycle ViewModel berhenti.
- **Perbaikan**: Ganti `throw` dengan logging error.

#### Bug 2.7: Main Thread Blocking & `ClassCastException` di `getService()`
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/MediaControllerViewModel.kt:168`
- **Deskripsi**: `sendCustomCommand(...).get().extras` dan cast ke `MusicService.MusicBinder`.
- **Penyebab Crash**: `.get()` membekukan UI thread. Jika extras null / salah tipe, cast melempar `ClassCastException`.
- **Perbaikan**: Gunakan listener asinkron dan safe casting `as? MusicService.MusicBinder`.

#### Bug 2.8: `UninitializedPropertyAccessException` pada `MediaLibrarySessionCallback`
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/MediaLibrarySessionCallback.kt:91`
- **Deskripsi**: `service` dideklarasikan sebagai `lateinit var service: MusicService`.
- **Penyebab Crash**: Mengakses `service` sebelum diinisialisasi oleh `MusicService.onCreate()` melempar `UninitializedPropertyAccessException`.
- **Perbaikan**: Ubah ke `var service: MusicService? = null` dan tambahkan null check.

#### Bug 2.9: `NullPointerException` pada Queue Resumption
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/MediaLibrarySessionCallback.kt:121`
- **Deskripsi**: `listOf(q.getCurrentSong()!!.toMediaItem())`.
- **Penyebab Crash**: Jika antrean kosong / `getCurrentSong()` mengembalikan null, `!!` melempar NPE.
- **Perbaikan**: Periksa `getCurrentSong()` secara aman sebelum membuat list item.

#### Bug 2.10: Unhandled `ExecutionException` pada Service Creation
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/MusicService.kt:281`
- **Deskripsi**: `controllerFuture.addListener({ controllerFuture.get() }, ...)` tanpa try-catch.
- **Penyebab Crash**: Jika pengikatan controller gagal, `.get()` melempar `ExecutionException` yang tidak tertangkap, mematikan service.
- **Perbaikan**: Bungkus `.get()` dengan block `try-catch`.

#### Bug 2.11: `NoSuchElementException` saat Simpan Empty Queue
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/MusicService.kt:599`
- **Deskripsi**: `data.last().lastSongPos = currentPosition` di mana `data = getAllQueues()`.
- **Penyebab Crash**: Jika `getAllQueues()` mengembalikan list kosong, `.last()` melempar `NoSuchElementException`.
- **Perbaikan**: Gunakan `data.lastOrNull()?.let { ... }`.

#### Bug 2.12: `IndexOutOfBoundsException` & NPE pada Stream Data Source (`MusicService`)
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/MusicService.kt:749-753`
- **Deskripsi**: `mimeType.split("codecs=")[1]` dan `format.contentLength!!`.
- **Penyebab Crash**: Sama dengan Bug 2.1, melempar IOOBE dan NPE jika metadata format tidak lengkap.
- **Perbaikan**: Gunakan `getOrNull(1)` dan safe null handling.

#### Bug 2.13: NPE & `NoSuchElementException` pada YouTube Auto-Load Next Page
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/MusicService.kt:972-973`
- **Deskripsi**: `q.playlistId = ...` (di mana `q` nullable) dan `.first()` pada list kosong.
- **Penyebab Crash**: Akses `q` tanpa safe call `?.` melempar NPE jika `q == null`. Mengakses `.first()` pada hasil list kosong melempar `NoSuchElementException`.
- **Perbaikan**: Periksa `q != null` dan `mediaItems.isNotEmpty()`.

#### Bug 2.14: `NullPointerException` pada `PlayerConnection` Instantiation
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/PlayerConnection.kt:52`
- **Deskripsi**: `val service = binder.getService()!!`.
- **Penyebab Crash**: jika `binder.getService()` mengembalikan null, `!!` melempar NPE.
- **Perbaikan**: Tangani kemungkinan service null.

#### Bug 2.15: `IndexOutOfBoundsException` di `QueueBoard.removeSong` & `shuffle`
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/QueueBoard.kt:411, 544`
- **Deskripsi**: `item.queue.removeAt(index)` dan `item.queue[item.queuePos]` tanpa validasi bounds.
- **Penyebab Crash**: Mengakses index di luar batas list melempar `IndexOutOfBoundsException`.
- **Perbaikan**: Periksa `index in 0 until item.queue.size` dan `queuePos >= 0`.

#### Bug 2.16: Non-Thread-Safe `PriorityQueue` Race Condition & Crash
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/QueueBoard.kt:839`
- **Deskripsi**: `PriorityQueue` diakses serentak dari coroutine IO tanpa sinkronisasi thread.
- **Penyebab Crash**: Mengubah `PriorityQueue` dari multiple thread memicu `ConcurrentModificationException` / `NullPointerException`.
- **Perbaikan**: Gunakan thread-safe structure atau bungkus dengan sinkronisasi Mutex.

#### Bug 2.17: `IllegalArgumentException` di ExoPlayer `replaceMediaItems`
- **File**: `app/src/main/java/com/dd3boh/outertune/playback/QueueBoard.kt:801`
- **Deskripsi**: `player.player.replaceMediaItems(queuePos + 1, Int.MAX_VALUE, ...)`.
- **Penyebab Crash**: `Int.MAX_VALUE` melebihi `mediaItemCount` ExoPlayer, melempar `IllegalArgumentException`.
- **Perbaikan**: Gunakan `player.player.mediaItemCount` sebagai pengganti `Int.MAX_VALUE`.

---

### 🗄️ Modul 3: ViewModels & Database

#### Bug 3.1: Singleton Database Connection Ditutup (`database.close()`)
- **File**: `app/src/main/java/com/dd3boh/outertune/viewmodels/BackupRestoreViewModel.kt:81 & 105`
- **Deskripsi**: `database.close()` dipanggil pada instance Hilt singleton `MusicDatabase`.
- **Penyebab Crash**: Semua akses database berikutnya di seluruh aplikasi akan melempar `IllegalStateException: database cannot be opened after being closed`.
- **Perbaikan**: Hapus `database.close()`, lakukan restore melalui file temporary tanpa menutup instance Room global.

#### Bug 3.2: Thread Freeze / ANR pada `runBlocking` + `Flow.first()` di `SongsDao`
- **File**: `app/src/main/java/com/dd3boh/outertune/db/daos/SongsDao.kt:378`
- **Deskripsi**: `runBlocking { getPlayCountByMonth(...).first() }`.
- **Penyebab Crash**: Ketika 0 row ditemukan (lagu baru diputar), Room Flow tidak pernah emisi nilai dan menunggu invalidation. `runBlocking` memblokir UI thread selamanya -> **ANR Crash**.
- **Perbaikan**: Buat query `suspend` langsung di DAO yang mengembalikan `Int?` tanpa Flow dan tanpa `runBlocking`.

#### Bug 3.3: Invalid SQL Query `WHERE id NOT IN ()` & Coroutine Unmanaged di Queue DAO
- **File**: `app/src/main/java/com/dd3boh/outertune/db/daos/QueueDao.kt:115`
- **Deskripsi**: `nukeAliens` memanggil `DELETE FROM queue WHERE id NOT IN (:ids)` dengan list `ids` kosong, serta `CoroutineScope(Dispatchers.IO).launch` di dalam method `@Transaction`.
- **Penyebab Crash**: `WHERE id NOT IN ()` melempar `SQLiteException: near ")": syntax error`. Karena di dalam unmanaged coroutine, melempar uncaught background crash.
- **Perbaikan**: Ubah `updateAllQueues` menjadi `suspend fun`. Jika `ids` kosong, jalankan `deleteAllQueues()`.

#### Bug 3.4: Infinite Loop Network di `OnlinePlaylistViewModel`
- **File**: `app/src/main/java/com/dd3boh/outertune/viewmodels/OnlinePlaylistViewModel.kt:62-73`
- **Deskripsi**: `while (continuation != null)` di mana error handling tidak mengosongkan `continuation`.
- **Penyebab Crash**: Ketika koneksi terputus, loop berjalan tanpa henti (100% CPU usage), membekukan aplikasi dan memicu OutOfMemory crash.
- **Perbaikan**: Set `continuation = null` saat terjadi error.

#### Bug 3.5: Direct `!!` Unwrapping pada `SavedStateHandle` Arguments
- **File**: `AlbumViewModel.kt:23`, `ArtistViewModel.kt:25`, `LocalPlaylistViewModel.kt:32`, dsb (10 ViewModel files)
- **Deskripsi**: `savedStateHandle.get<String>("key")!!`.
- **Penyebab Crash**: Jika argumen navigasi hilang saat rekreatif proses Android, `get()` mengembalikan null. Operator `!!` melempar `NullPointerException`.
- **Perbaikan**: Tangani nilai null secara aman dengan fallback `?: ""`.

#### Bug 3.6: Off-Main-Thread Mutation of Compose `SnapshotStateList`
- **File**: `app/src/main/java/com/dd3boh/outertune/viewmodels/LibraryViewModels.kt:158`
- **Deskripsi**: `filteredSongs.clear()` & `filteredSongs.addAll()` dipanggil di `Dispatchers.IO`.
- **Penyebab Crash**: Mengubah `SnapshotStateList` Compose di background thread saat UI membaca list memicu `ConcurrentModificationException` atau snapshot corruption crash.
- **Perbaikan**: Lakukan pencarian di `Dispatchers.IO`, lalu update state di `Dispatchers.Main`.

#### Bug 3.7: Off-Main-Thread Modification of Compose `MutableState`
- **File**: `app/src/main/java/com/dd3boh/outertune/viewmodels/HistoryViewModel.kt:63`
- **Deskripsi**: `historyPage.value = it` di dalam `viewModelScope.launch(Dispatchers.IO)`.
- **Penyebab Crash**: Menulis Compose `MutableState` dari background thread berisiko memicu race condition dan Compose rendering crash.
- **Perbaikan**: Lakukan update state pada Main Thread (`Dispatchers.Main.immediate`).

#### Bug 3.8: Infinite Database Re-emission Feedback Loop
- **File**: `LibraryViewModels.kt:194, 243` & `StatsViewModel.kt:47`
- **Deskripsi**: Di dalam `Flow.collect`, kode mengeksekusi `database.query { update(...) / delete(...) }` pada tabel yang sama.
- **Penyebab Crash**: Menulis ke tabel di dalam listener Flow tabel tersebut memaksa Room mengemisi ulang Flow tanpa henti, memicu infinite loop CPU & disk write.
- **Perbaikan**: Ambil snapshot tunggal via `.first()` atau pisahkan sync background dari observer Flow.

#### Bug 3.9: State Management Asinkron `isLoading` Rusak
- **File**: `app/src/main/java/com/dd3boh/outertune/viewmodels/OnlinePlaylistViewModel.kt:51`
- **Deskripsi**: `isLoading.value = false` dieksekusi secara sinkron di luar coroutine launch `getContinuation()`.
- **Penyebab Crash**: Status `isLoading` langsung kembali false sebelum task selesai, memungkinkan user menekan tombol berulang kali dan memicu balapan coroutine.
- **Perbaikan**: Pindahkan `isLoading.value = false` ke block `finally` di dalam coroutine.

#### Bug 3.10: Unmanaged Coroutine Scope Calling UI Callback Off-Main Thread
- **File**: `app/src/main/java/com/dd3boh/outertune/viewmodels/LyricsMenuViewModel.kt:60`
- **Deskripsi**: `CoroutineScope(Dispatchers.IO).launch` memanggil `onDone(lyrics)`.
- **Penyebab Crash**: Memanggil UI callback dari IO thread di luar lifecycle ViewModel berisiko memicu UI thread assertion failure.
- **Perbaikan**: Gunakan `viewModelScope.launch` dan switch ke `Dispatchers.Main`.

---

### 🎨 Modul 4: UI, Models & Utilities

#### Bug 4.1: Unsafe `ClassCastException` di `TagLibScanner`
- **File**: `app/src/main/java/com/dd3boh/outertune/utils/scanners/TagLibScanner.kt:196`
- **Deskripsi**: `artistList.distinctBy { ... } as ArrayList<ArtistEntity>`.
- **Penyebab Crash**: Jika list kosong, `distinctBy` mengembalikan `kotlin.collections.EmptyList`. Cast ke `ArrayList` melempar `ClassCastException`.
- **Perbaikan**: Gunakan `ArrayList(artistList.filterNot { ... }.distinctBy { ... })`.

#### Bug 4.2: Unhandled NPE & Native Resource Leak di `TagLibScanner`
- **File**: `app/src/main/java/com/dd3boh/outertune/utils/scanners/TagLibScanner.kt:68`
- **Deskripsi**: `TagLib.getAudioProperties(...)!!` dan `TagLib.getMetadata(...)!!`.
- **Penyebab Crash**: Jika file media korup/tidak didukung, TagLib mengembalikan null, `!!` melempar NPE.
- **Perbaikan**: Periksa null secara aman dan lemparkan exception yang ter-catch.

#### Bug 4.3: `IndexOutOfBoundsException` pada `MultiQueueObject` Kosong
- **File**: `app/src/main/java/com/dd3boh/outertune/models/MultiQueueObject.kt:49`
- **Deskripsi**: `getCurrentSong()` mengakses `queue[queuePos]` saat `queue.isEmpty()`.
- **Penyebab Crash**: Melempar `IndexOutOfBoundsException: Index: 0, Size: 0`.
- **Perbaikan**: Kembalikan `null` jika `queue.isEmpty()`.

#### Bug 4.4: `NoSuchElementException` di `DirectoryTree.getSong`
- **File**: `app/src/main/java/com/dd3boh/outertune/models/DirectoryTree.kt:132`
- **Deskripsi**: `files.first { ... }`.
- **Penyebab Crash**: Jika lagu tidak ditemukan dalam direktori, `.first` melempar `NoSuchElementException`.
- **Perbaikan**: Ganti `.first` dengan `.firstOrNull`.

#### Bug 4.5: NPE Menekan Lagu Tanpa Album di `HomeScreen`
- **File**: `app/src/main/java/com/dd3boh/outertune/ui/screens/HomeScreen.kt:614`
- **Deskripsi**: `navController.navigate("album/${it.title.album!!.id}")`.
- **Penyebab Crash**: Lagu tanpa album metadata (`album == null`) melempar NPE saat ditekan.
- **Perbaikan**: Gunakan `it.title.album?.id?.let { id -> navController.navigate("album/$id") }`.

#### Bug 4.6: `UninitializedPropertyAccessException` di `PoTokenWebView`
- **File**: `app/src/main/java/com/dd3boh/outertune/utils/potoken/PoTokenWebView.kt:219`
- **Deskripsi**: `Instant.now().isAfter(expirationInstant)` di mana `expirationInstant` `lateinit var`.
- **Penyebab Crash**: Mengakses `expirationInstant` sebelum inisialisasi selesai melempar `UninitializedPropertyAccessException`.
- **Perbaikan**: Periksa `!::expirationInstant.isInitialized` terlebih dahulu.

#### Bug 4.7: Native Memory & File Descriptor Leak di `CoilBitmapLoader`
- **File**: `app/src/main/java/com/dd3boh/outertune/utils/CoilBitmapLoader.kt:94-101`
- **Deskripsi**: `MediaMetadataRetriever()` dibuat tanpa `release()` / `close()`, plus `art!!.size`.
- **Penyebab Crash**: File descriptor habis (`EMFILE`), memicu Native OOM Crash. `art!!.size` melempar NPE jika artwork null.
- **Perbaikan**: Gunakan `.use { }` dan periksa `art != null`.

#### Bug 4.8: Connection Socket Leak di `YTPlayerUtils`
- **File**: `app/src/main/java/com/dd3boh/outertune/utils/YTPlayerUtils.kt:255`
- **Deskripsi**: `httpClient.newCall(...).execute()` tanpa menutup `Response`.
- **Penyebab Crash**: Socket leak dan pool connection exhaustion memicu timeout/socket crash saat pemutaran.
- **Perbaikan**: Bungkus eksekusi dengan `execute().use { response -> ... }`.

#### Bug 4.9: `NoSuchElementException` pada Penentuan Warna Tema Dinamis
- **File**: `app/src/main/java/com/dd3boh/outertune/ui/theme/Theme.kt:145`
- **Deskripsi**: `rankedColors.first()` pada hasil ekstraksi Palette.
- **Penyebab Crash**: Jika gambar transparan/polos, `rankedColors` kosong, `.first()` melempar `NoSuchElementException`.
- **Perbaikan**: Gunakan `rankedColors.firstOrNull()`.

#### Bug 4.10: NPE pada Queue Total Duration Calculation
- **File**: `app/src/main/java/com/dd3boh/outertune/ui/player/Queue.kt:971`
- **Deskripsi**: `queueWindows.sumOf { it.mediaItem.metadata!!.duration }`.
- **Penyebab Crash**: `metadata` bisa null pada MediaItem dasar, `!!` melempar NPE.
- **Perbaikan**: Gunakan `it.mediaItem.metadata?.duration ?: 0`.

#### Bug 4.11: NPE di `YouTubePlaylistMenu` Shuffle Click
- **File**: `app/src/main/java/com/dd3boh/outertune/ui/menu/YouTubePlaylistMenu.kt:196`
- **Deskripsi**: `playlist.playEndpoint!!.playlistId` di dalam callback `shuffleEndpoint`.
- **Penyebab Crash**: Jika `playEndpoint` null, menekan tombol Shuffle melempar NPE.
- **Perbaikan**: Ambil `playlistId` dari `shuffleEndpoint` atau safe unwrap `playEndpoint`.

#### Bug 4.12: NPE di `ArtistSongsScreen` Play Action
- **File**: `app/src/main/java/com/dd3boh/outertune/ui/screens/artist/ArtistSongsScreen.kt:206`
- **Deskripsi**: `YouTube.artist(artist?.id!!).getOrNull()`.
- **Penyebab Crash**: Jika `artist` null, `artist?.id` mengembalikan null, `(null)!!` melempar NPE.
- **Perbaikan**: Periksa `val artistId = artist?.id ?: return@launch`.

#### Bug 4.13: NPE di `LocalPlaylistScreen` Dialog Confirmation
- **File**: `app/src/main/java/com/dd3boh/outertune/ui/screens/playlist/LocalPlaylistScreen.kt:276 & 322`
- **Deskripsi**: `playlistWithSongs.first?.playlist!!.name`.
- **Penyebab Crash**: Jika `playlistWithSongs.first` null, `playlistWithSongs.first?.playlist` bernilai null, `null!!` melempar NPE.
- **Perbaikan**: Gunakan `playlistWithSongs.first?.playlist?.name ?: ""`.

#### Bug 4.14: NPE di `AddToPlaylistDialog` Item Click
- **File**: `app/src/main/java/com/dd3boh/outertune/ui/dialog/AddToPlaylistDialog.kt:190, 195, 249, 264`
- **Deskripsi**: `database.playlistDuplicates(playlist.id, songIds!!)`.
- **Penyebab Crash**: Jika `songIds` null, `songIds!!` melempar NPE saat playlist ditekan.
- **Perbaikan**: Periksa `val currentSongIds = songIds ?: return@launch`.

#### Bug 4.15: Main Thread Blocking dengan `runBlocking` di `SyncUtils`
- **File**: `app/src/main/java/com/dd3boh/outertune/utils/SyncUtils.kt:192, 256, 266, 325, 335, 407, 417, 491, 501, 559, 626`
- **Deskripsi**: `SyncUtils` membungkus panggilan I/O network dalam `runBlocking { ... launch(Dispatchers.IO) ... }`.
- **Penyebab Crash**: Memblokir UI thread hingga operasi network selesai memicu `NetworkOnMainThreadException` atau ANR.
- **Perbaikan**: Ganti `runBlocking` dengan `coroutineScope` atau `withContext(Dispatchers.IO)`.

---

*Laporan dibuat secara otomatis oleh Antigravity Bug Hunter pada 2026-08-05.*
