/*
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.dd3boh.outertune.db.MusicDatabase.Companion.MUSIC_DATABASE_VERSION
import com.dd3boh.outertune.db.InternalDatabase
import com.dd3boh.outertune.utils.reportException
import java.io.File

/**
 * leafx - Legacy Interface untuk Menghapus Database Corrupt dan Sesi Corrupt.
 *
 * Dipanggil paling awal di App.onCreate(), sebelum Hilt/Room/ExoPlayer menyentuh
 * storage apa pun. Tugasnya memastikan aplikasi tidak crash-loop karena state
 * di disk yang rusak atau tidak kompatibel:
 *
 *  1. Database musik (song.db): karantina bila identity hash Room tidak cocok,
 *     versi tak terbaca, atau file korup.
 *  2. Sesi pemutar: hapus database internal ExoPlayer (indeks SimpleCache) dan
 *     isi folder cache player/download bila tidak bisa dibuka.
 *
 * Semua data dikarantina (di-rename ke *.bak-corrupt), bukan langsung dihapus,
 * agar masih bisa dipulihkan manual.
 */
object Leafx {
    private const val TAG = "leafx"

    // Identity hash skema Room yang diharapkan pada MUSIC_DATABASE_VERSION saat ini.
    // Perbarui setiap kali entity berubah tanpa menaikkan nomor versi.
    private const val EXPECTED_IDENTITY_HASH = "1f19684463889aeea6f5078a8d5285a0"

    /**
     * Entry point utama. Aman dipanggil kapan pun; idempoten dan tidak akan melempar.
     */
    fun handle(context: Context) {
        runCatching { recoverDatabase(context) }
            .onFailure { Log.w(TAG, "database recovery failed: ${it.message}") }
        runCatching { recoverSessions(context) }
            .onFailure { Log.w(TAG, "session recovery failed: ${it.message}") }
    }

    /**
     * Periksa song.db sebelum Room membukanya. Bila dibuat oleh build dengan skema
     * berbeda pada nomor versi yang sama, Room tidak bisa migrasi dan melempar
     * IllegalStateException -> crash loop. Karantina supaya DB baru dibuat.
     */
    fun recoverDatabase(context: Context) {
        val dbFile = context.getDatabasePath(InternalDatabase.DB_NAME)
        if (!dbFile.exists()) return

        val foundHash: String?
        try {
            val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            foundHash = try {
                val version = db.version
                if (version != MUSIC_DATABASE_VERSION) {
                    // Versi berbeda: jalur migrasi normal tersedia, biarkan Room menangani
                    return
                }
                db.rawQuery("SELECT identity_hash FROM room_master_table", null).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
            } finally {
                db.close()
            }
        } catch (e: Exception) {
            // Tidak bisa dibaca sama sekali: legacy tanpa room_master_table atau file korup
            Log.w(TAG, "${dbFile.name} unreadable (${e.message}), quarantining")
            quarantine(dbFile)
            return
        }

        if (foundHash == EXPECTED_IDENTITY_HASH) return

        Log.w(
            TAG,
            "Schema identity mismatch (expected=$EXPECTED_IDENTITY_HASH, found=$foundHash), quarantining"
        )
        reportException(IllegalStateException("Database schema mismatch: $foundHash"))
        quarantine(dbFile)
    }

    /**
     * Bersihkan sesi pemutar: indeks SimpleCache milik ExoPlayer disimpan di
     * exoplayer_internal.db. Bila korup, SimpleCache dapat gagal saat inisialisasi
     * dan membuat proses mati saat startup. Indeks korup -> hapus db + isi cache
     * terkait (cache hanya sekunder, aman dibangun ulang).
     */
    fun recoverSessions(context: Context) {
        val exoDb = context.getDatabasePath("exoplayer_internal.db")

        val corrupted = if (exoDb.exists()) !isReadableDatabase(exoDb) else false

        if (corrupted) {
            Log.w(TAG, "${exoDb.name} corrupt, removing session state")
            deleteTree(exoDb)
            // Indeks dan isi cache harus konsisten; buang keduanya
            clearCacheDir(File(context.filesDir, "exoplayer"))
            clearCacheDir(File(context.filesDir, "download"))
        }
    }

    private fun isReadableDatabase(file: File): Boolean =
        try {
            val db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            try {
                db.rawQuery("PRAGMA integrity_check(1)", null).use { it.moveToFirst() }
            } finally {
                db.close()
            }
        } catch (e: Exception) {
            false
        }

    /**
     * Pindahkan database beserta sidecar WAL/SHM ke nama *.bak-corrupt agar
     * dibuat file baru. Backup lama dengan nama sama ditimpa.
     */
    private fun quarantine(dbFile: File) {
        for (suffix in listOf("", "-wal", "-shm")) {
            val file = File(dbFile.path + suffix)
            if (!file.exists()) continue
            val backup = File(dbFile.path + suffix + ".bak-corrupt")
            if (backup.exists()) backup.delete()
            if (!file.renameTo(backup)) file.delete()
        }
    }

    private fun clearCacheDir(dir: File) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { child ->
            if (!child.deleteRecursively()) {
                Log.w(TAG, "Failed to clear ${child.path}")
            }
        }
    }

    private fun deleteTree(file: File) {
        for (suffix in listOf("", "-wal", "-shm", "-journal")) {
            val f = File(file.path + suffix)
            if (f.exists() && !f.delete()) {
                Log.w(TAG, "Failed to delete ${f.path}")
            }
        }
    }
}
