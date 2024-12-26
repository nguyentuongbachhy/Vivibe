package com.example.vivibe.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "youtube_music.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_SONG_PLAY_HISTORY = "songPlayHistory"
        private const val COLUMN_SONG_ID = "song_id"
        private const val COLUMN_GOOGLE_ID = "google_id"
        private const val COLUMN_PLAY_COUNT = "play_count"
        private const val COLUMN_LAST_PLAYED = "last_played"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE $TABLE_SONG_PLAY_HISTORY (
                    $COLUMN_SONG_ID INTEGER,
                    $COLUMN_GOOGLE_ID TEXT,
                    $COLUMN_PLAY_COUNT INTEGER DEFAULT 1,
                    $COLUMN_LAST_PLAYED DATETIME DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY ($COLUMN_SONG_ID, $COLUMN_GOOGLE_ID)
                )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SONG_PLAY_HISTORY")
        onCreate(db)
    }

    fun insertOrUpdateSongPlayHistory(songId: Int, googleId: String) {
        if(googleId.isNotBlank()) {
            val db = writableDatabase
            try {
                val cursor: Cursor = db.rawQuery(
                    """
                    SELECT $COLUMN_PLAY_COUNT 
                    FROM $TABLE_SONG_PLAY_HISTORY
                    WHERE $COLUMN_SONG_ID = ? AND $COLUMN_GOOGLE_ID = ?
                """.trimIndent(), arrayOf(songId.toString(), googleId)
                )

                val playCount = if (cursor.moveToFirst()) cursor.getInt(0) + 1 else 1
                cursor.close()

                val values = ContentValues().apply {
                    put(COLUMN_SONG_ID, songId)
                    put(COLUMN_GOOGLE_ID, googleId)
                    put(COLUMN_PLAY_COUNT, playCount)
                    put(COLUMN_LAST_PLAYED, System.currentTimeMillis())
                }

                db.insertWithOnConflict(
                    TABLE_SONG_PLAY_HISTORY,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.close()
            }
        }
    }

    @SuppressLint("Recycle")
    fun getTopSongs(googleId: String, limit: Int): List<Int> {
        if(googleId.isNotBlank()) {
            val db = readableDatabase
            val songIds = mutableListOf<Int>()

            val cursor: Cursor? = try {
                db.rawQuery(
                    """
                   SELECT $COLUMN_SONG_ID
                    FROM $TABLE_SONG_PLAY_HISTORY
                    WHERE $COLUMN_GOOGLE_ID = ?
                    AND $COLUMN_GOOGLE_ID IS NOT NULL 
                    AND $COLUMN_GOOGLE_ID != ''
                    ORDER BY $COLUMN_LAST_PLAYED DESC, $COLUMN_PLAY_COUNT DESC
                    LIMIT ?
                """.trimIndent(), arrayOf(googleId, limit.toString())
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return emptyList()
            }

            cursor?.use {
                while (it.moveToNext()) {
                    songIds.add(it.getInt(0))
                }
            }
            db.close()
            return songIds
        }
        else {
            return emptyList()
        }
    }
}
