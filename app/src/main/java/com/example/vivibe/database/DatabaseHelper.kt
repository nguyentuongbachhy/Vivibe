package com.example.vivibe.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.vivibe.model.DownloadedSong

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "youtube_music.db"
        private const val DATABASE_VERSION = 1

        // Table and column names
        private object Tables {
            const val SONG_PLAY_HISTORY = "songPlayHistory"
            const val ARTIST_SONG_PLAY = "artistSongPlay"
            const val DOWNLOADED_SONGS = "downloadedSongs"
        }

        private object Columns {
            const val SONG_ID = "songId"
            const val GOOGLE_ID = "googleId"
            const val PLAY_COUNT = "playCount"
            const val ARTIST_ID = "artistId"
            const val LAST_PLAYED = "lastPlayed"
            const val LIKED = "liked"
            const val DISLIKED = "disliked"
            const val FOLLOWED = "followed"
            const val TITLE = "title"
            const val THUMBNAIL_PATH = "thumbnailPath"
            const val ARTIST_NAME = "artistName"
            const val AUDIO_PATH = "audioPath"
            const val DURATION = "duration"
            const val LYRICS = "lyrics"
            const val VIEWS = "views"
            const val LIKES = "likes"
            const val DOMINANT_COLOR = "dominantColor"
            const val DOWNLOAD_DATE = "downloadDate"
        }

        // SQL Queries
        private object Queries {
            const val CREATE_DOWNLOADED_SONGS_TABLE = """
                CREATE TABLE ${Tables.DOWNLOADED_SONGS} (
                    ${Columns.SONG_ID} INTEGER PRIMARY KEY,
                    ${Columns.GOOGLE_ID} TEXT NOT NULL,
                    ${Columns.TITLE} TEXT NOT NULL,
                    ${Columns.THUMBNAIL_PATH} TEXT NOT NULL,
                    ${Columns.ARTIST_ID} INTEGER NOT NULL,
                    ${Columns.ARTIST_NAME} TEXT NOT NULL,
                    ${Columns.AUDIO_PATH} TEXT NOT NULL,
                    ${Columns.DURATION} INTEGER NOT NULL,
                    ${Columns.LYRICS} TEXT NOT NULL,
                    ${Columns.VIEWS} INTEGER NOT NULL,
                    ${Columns.LIKES} INTEGER NOT NULL,
                    ${Columns.DOMINANT_COLOR} INTEGER NOT NULL,
                    ${Columns.DOWNLOAD_DATE} INTEGER NOT NULL
                )
            """
            const val CREATE_SONG_HISTORY_TABLE = """
                CREATE TABLE ${Tables.SONG_PLAY_HISTORY} (
                    ${Columns.SONG_ID} INTEGER,
                    ${Columns.GOOGLE_ID} TEXT,
                    ${Columns.PLAY_COUNT} INTEGER DEFAULT 1,
                    ${Columns.LIKED} BOOLEAN DEFAULT False,
                    ${Columns.DISLIKED} BOOLEAN DEFAULT False,
                    ${Columns.LAST_PLAYED} DATETIME DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (${Columns.SONG_ID}, ${Columns.GOOGLE_ID})
                )
            """

            const val CREATE_ARTIST_SONG_PLAY_TABLE = """
                CREATE TABLE ${Tables.ARTIST_SONG_PLAY} (
                    ${Columns.ARTIST_ID} INTEGER,
                    ${Columns.GOOGLE_ID} TEXT,
                    ${Columns.PLAY_COUNT} INTEGER DEFAULT 1,
                    ${Columns.FOLLOWED} BOOLEAN DEFAULT False,
                    ${Columns.LAST_PLAYED} DATETIME DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (${Columns.ARTIST_ID}, ${Columns.GOOGLE_ID})
                )
            """

            const val GET_PLAY_COUNT = """
                SELECT ${Columns.PLAY_COUNT}
                FROM ${Tables.SONG_PLAY_HISTORY}
                WHERE ${Columns.SONG_ID} = ? AND ${Columns.GOOGLE_ID} = ?
            """

            const val GET_FORGOTTEN_FAVORITES = """
                SELECT ${Columns.SONG_ID}
                FROM ${Tables.SONG_PLAY_HISTORY}
                WHERE ${Columns.GOOGLE_ID} = ?
                ORDER BY ${Columns.LAST_PLAYED}
                LIMIT ?
            """

            const val GET_TOP_SONGS = """
                SELECT ${Columns.SONG_ID}
                FROM ${Tables.SONG_PLAY_HISTORY}
                WHERE ${Columns.GOOGLE_ID} = ?
                AND ${Columns.PLAY_COUNT} > 5
                ORDER BY ${Columns.PLAY_COUNT} DESC
                LIMIT ?
            """

            const val GET_TOP_ARTISTS = """
                SELECT ${Columns.ARTIST_ID}
                FROM ${Tables.ARTIST_SONG_PLAY}
                WHERE ${Columns.GOOGLE_ID} = ?
                ORDER BY ${Columns.PLAY_COUNT} DESC
                LIMIT ?
            """

            const val GET_LIKED_SONGS = """
                SELECT ${Columns.LIKED}
                FROM ${Tables.SONG_PLAY_HISTORY}
                WHERE ${Columns.GOOGLE_ID} = ?
                AND ${Columns.SONG_ID} = ?
            """

            const val UPDATE_LIKED_STATUS = """
                UPDATE ${Tables.SONG_PLAY_HISTORY}
                SET ${Columns.LIKED} = ?
                WHERE ${Columns.GOOGLE_ID} = ? 
                AND ${Columns.SONG_ID} = ?
            """

            const val GET_DISLIKED_SONGS = """
                SELECT ${Columns.DISLIKED}
                FROM ${Tables.SONG_PLAY_HISTORY}
                WHERE ${Columns.GOOGLE_ID} = ?
                AND ${Columns.SONG_ID} = ?
            """

            const val UPDATE_DISLIKED_STATUS = """
                UPDATE ${Tables.SONG_PLAY_HISTORY}
                SET ${Columns.DISLIKED} = ?
                WHERE ${Columns.GOOGLE_ID} = ? 
                AND ${Columns.SONG_ID} = ?
            """

            const val GET_FOLLOWED_ARTISTS = """
                SELECT ${Columns.FOLLOWED}
                FROM ${Tables.ARTIST_SONG_PLAY}
                WHERE ${Columns.GOOGLE_ID} = ?
                AND ${Columns.ARTIST_ID} = ?
            """

        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Queries.CREATE_SONG_HISTORY_TABLE)
        db.execSQL(Queries.CREATE_ARTIST_SONG_PLAY_TABLE)
        db.execSQL(Queries.CREATE_DOWNLOADED_SONGS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Tables.SONG_PLAY_HISTORY}")
        db.execSQL("DROP TABLE IF EXISTS ${Tables.ARTIST_SONG_PLAY}")
        db.execSQL("DROP TABLE IF EXISTS ${Tables.DOWNLOADED_SONGS}")
        onCreate(db)
    }

    /**
     * Inserts or updates a song play history record.
     * @param songId The ID of the song
     * @param googleId The Google account ID
     * @return Boolean indicating if the operation was successful
     */
    fun insertOrUpdateSongPlayHistory(songId: Int, googleId: String): Boolean {
        if (googleId.isBlank()) return false

        return try {
            writableDatabase.use { db ->
                db.rawQuery(Queries.GET_PLAY_COUNT, arrayOf(songId.toString(), googleId)).use { cursor ->
                    val playCount = if (cursor.moveToFirst()) cursor.getInt(0) + 1 else 1

                    val values = ContentValues().apply {
                        put(Columns.SONG_ID, songId)
                        put(Columns.GOOGLE_ID, googleId)
                        put(Columns.PLAY_COUNT, playCount)
                        put(Columns.LAST_PLAYED, System.currentTimeMillis())
                    }

                    db.insertWithOnConflict(
                        Tables.SONG_PLAY_HISTORY,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Inserts or updates an artist play history record.
     * @param artistId The ID of the artist
     * @param googleId The Google account ID
     * @return Boolean indicating if the operation was successful
     */
    fun insertOrUpdateArtistPlayHistory(artistId: Int, googleId: String): Boolean {
        if (googleId.isBlank()) return false

        return try {
            writableDatabase.use { db ->
                // Get current play count
                db.rawQuery(
                    """
                SELECT ${Columns.PLAY_COUNT}
                FROM ${Tables.ARTIST_SONG_PLAY}
                WHERE ${Columns.ARTIST_ID} = ? AND ${Columns.GOOGLE_ID} = ?
                """,
                    arrayOf(artistId.toString(), googleId)
                ).use { cursor ->
                    val playCount = if (cursor.moveToFirst()) cursor.getInt(0) + 1 else 1

                    val values = ContentValues().apply {
                        put(Columns.ARTIST_ID, artistId)
                        put(Columns.GOOGLE_ID, googleId)
                        put(Columns.PLAY_COUNT, playCount)
                        put(Columns.LAST_PLAYED, System.currentTimeMillis())
                    }

                    db.insertWithOnConflict(
                        Tables.ARTIST_SONG_PLAY,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Gets a list of songs that haven't been played recently.
     * @param googleId The Google account ID
     * @param limit The maximum number of songs to return (default: 10)
     * @return List of song IDs
     */
    fun getForgottenFavorites(googleId: String, limit: Int = 5): List<Int> {
        if (googleId.isBlank()) return emptyList()

        return try {
            readableDatabase.use { db ->
                db.rawQuery(Queries.GET_FORGOTTEN_FAVORITES, arrayOf(googleId, limit.toString())).use { cursor ->
                    cursor.toSongIdList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Gets the most played songs for a user.
     * @param googleId The Google account ID
     * @param limit The maximum number of songs to return
     * @return List of song IDs
     */
    fun getTopSongs(googleId: String, limit: Int): List<Int> {
        if (googleId.isBlank()) return emptyList()

        return try {
            readableDatabase.use { db ->
                db.rawQuery(Queries.GET_TOP_SONGS, arrayOf(googleId, limit.toString())).use { cursor ->
                    cursor.toSongIdList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Gets artists that a for a user.
     * @param googleId The Google account ID
     * @param limit The maximum number of songs to return
     * @return List of song IDs
     */
    fun getTopArtists(googleId: String, limit: Int): List<Int> {
        if (googleId.isBlank()) return emptyList()

        return try {
            readableDatabase.use { db ->
                db.rawQuery(Queries.GET_TOP_ARTISTS, arrayOf(googleId, limit.toString())).use { cursor ->
                    cursor.toSongIdList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Gets liked status of a song for a user.
     * @param googleId The Google account ID
     * @param songId The song ID to check
     * @return Boolean indicating if the song is liked, false if error or not found
     */
    fun isLikedSong(googleId: String, songId: Int): Boolean {
        if (googleId.isBlank()) return false

        return try {
            readableDatabase.use { db ->
                db.rawQuery(Queries.GET_LIKED_SONGS, arrayOf(googleId, songId.toString())).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getInt(0) != 0 else false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Updates the liked status of a song for a specific user
     * @param googleId The Google account ID
     * @param songId The ID of the song to update
     * @param liked New liked status
     * @return Boolean indicating if update was successful
     */
    fun updateLikedStatus(googleId: String, songId: Int, liked: Boolean): Boolean {
        if (googleId.isBlank()) {
            Log.d("DatabaseHelper", "Google ID is blank")
            return false
        }

        return try {
            writableDatabase.use { db ->
                Log.d("DatabaseHelper", "Updating like status: googleId=$googleId, songId=$songId, liked=$liked")
                db.execSQL(
                    Queries.UPDATE_LIKED_STATUS,
                    arrayOf(liked, googleId, songId)
                )
                Log.d("DatabaseHelper", "Like status updated successfully")
                true
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error updating like status", e)
            e.printStackTrace()
            false
        }
    }

    /**
     * Gets disliked status of a song for a user.
     * @param googleId The Google account ID
     * @param songId The song ID to check
     * @return Boolean indicating if the song is disliked, false if error or not found
     */
    fun isDislikedSong(googleId: String, songId: Int): Boolean {
        if (googleId.isBlank()) return false

        return try {
            readableDatabase.use { db ->
                db.rawQuery(Queries.GET_DISLIKED_SONGS, arrayOf(googleId, songId.toString())).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getInt(0) != 0 else false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Updates the disliked status of a song for a specific user
     * @param googleId The Google account ID
     * @param songId The ID of the song to update
     * @param disliked New disliked status
     * @return Boolean indicating if update was successful
     */
    fun updateDislikedStatus(googleId: String, songId: Int, disliked: Boolean): Boolean {
        if (googleId.isBlank()) return false

        return try {
            writableDatabase.use { db ->
                db.execSQL(
                    Queries.UPDATE_DISLIKED_STATUS,
                    arrayOf(disliked, googleId, songId)
                )
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /**
     * Gets followed status of an artist for a user.
     * @param googleId The Google account ID
     * @param artistId The artist ID to check
     * @return Boolean indicating if the artist is followed, false if error or not found
     */
    fun isFollowedArtist(googleId: String, artistId: Int): Boolean {
        if (googleId.isBlank()) return false

        return try {
            readableDatabase.use { db ->
                db.rawQuery(Queries.GET_FOLLOWED_ARTISTS, arrayOf(googleId, artistId.toString())).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getInt(0) != 0 else false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun insertDownloadedSong(googleId: String, song: DownloadedSong): Boolean {
        return try {
            writableDatabase.use { db ->
                // Begin transaction for better performance and reliability
                db.beginTransaction()
                try {
                    val values = ContentValues().apply {
                        put(Columns.SONG_ID, song.id)
                        put(Columns.GOOGLE_ID, googleId)
                        put(Columns.TITLE, song.title)
                        put(Columns.THUMBNAIL_PATH, song.thumbnailPath)
                        put(Columns.ARTIST_ID, song.artistId)
                        put(Columns.ARTIST_NAME, song.artistName)
                        put(Columns.AUDIO_PATH, song.audioPath)
                        put(Columns.DURATION, song.duration)
                        put(Columns.LYRICS, song.lyrics)
                        put(Columns.VIEWS, song.views)
                        put(Columns.LIKES, song.likes)
                        put(Columns.DOMINANT_COLOR, song.dominantColor)
                        put(Columns.DOWNLOAD_DATE, System.currentTimeMillis())
                    }

                    val id = db.insertWithOnConflict(
                        Tables.DOWNLOADED_SONGS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                    )

                    Log.d("DatabaseHelper", "Insert result: $id for songId: ${song.id}")

                    if (id != -1L) {
                        db.setTransactionSuccessful()
                        true
                    } else {
                        false
                    }
                } finally {
                    db.endTransaction()
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error inserting downloaded song", e)
            false
        }
    }

    fun getDownloadedSong(googleId: String, songId: Int): DownloadedSong? {
        return try {
            readableDatabase.use { db ->
                db.query(
                    Tables.DOWNLOADED_SONGS,
                    null,
                    "${Columns.SONG_ID} = ? AND ${Columns.GOOGLE_ID} = ?",
                    arrayOf(songId.toString(), googleId),
                    null,
                    null,
                    null
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        DownloadedSong(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.SONG_ID)),
                            title = cursor.getString(cursor.getColumnIndexOrThrow(Columns.TITLE)),
                            thumbnailPath = cursor.getString(cursor.getColumnIndexOrThrow(Columns.THUMBNAIL_PATH)),
                            artistId = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.ARTIST_ID)),
                            artistName = cursor.getString(cursor.getColumnIndexOrThrow(Columns.ARTIST_NAME)),
                            audioPath = cursor.getString(cursor.getColumnIndexOrThrow(Columns.AUDIO_PATH)),
                            duration = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.DURATION)),
                            lyrics = cursor.getString(cursor.getColumnIndexOrThrow(Columns.LYRICS)),
                            views = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.VIEWS)),
                            likes = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.LIKES)),
                            dominantColor = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.DOMINANT_COLOR)),
                            downloadDate = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.DOWNLOAD_DATE))
                        )
                    } else null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAllDownloadedSongs(googleId: String): List<DownloadedSong> {
        return try {
            readableDatabase.use { db ->
                db.query(
                    Tables.DOWNLOADED_SONGS,
                    null,
                    "${Columns.GOOGLE_ID} = ?",
                    arrayOf(googleId),
                    null,
                    null,
                    "${Columns.DOWNLOAD_DATE} DESC"
                ).use { cursor ->
                    val songs = mutableListOf<DownloadedSong>()
                    while (cursor.moveToNext()) {
                        songs.add(
                            DownloadedSong(
                                id = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.SONG_ID)),
                                title = cursor.getString(cursor.getColumnIndexOrThrow(Columns.TITLE)),
                                thumbnailPath = cursor.getString(cursor.getColumnIndexOrThrow(Columns.THUMBNAIL_PATH)),
                                artistId = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.ARTIST_ID)),
                                artistName = cursor.getString(cursor.getColumnIndexOrThrow(Columns.ARTIST_NAME)),
                                audioPath = cursor.getString(cursor.getColumnIndexOrThrow(Columns.AUDIO_PATH)),
                                duration = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.DURATION)),
                                lyrics = cursor.getString(cursor.getColumnIndexOrThrow(Columns.LYRICS)),
                                views = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.VIEWS)),
                                likes = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.LIKES)),
                                dominantColor = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.DOMINANT_COLOR)),
                                downloadDate = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.DOWNLOAD_DATE))
                            )
                        )
                    }
                    songs
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun deleteDownloadedSong(googleId: String, songId: Int): Boolean {
        return try {
            writableDatabase.use { db ->
                db.delete(
                    Tables.DOWNLOADED_SONGS,
                    "${Columns.SONG_ID} = ? AND ${Columns.GOOGLE_ID} = ?",
                    arrayOf(songId.toString(), googleId)
                ) > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Extension function to convert a cursor to a list of song IDs
     */
    private fun Cursor.toSongIdList(): List<Int> {
        val songIds = mutableListOf<Int>()
        while (moveToNext()) {
            songIds.add(getInt(0))
        }
        return songIds
    }
}