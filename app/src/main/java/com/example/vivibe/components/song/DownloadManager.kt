package com.example.vivibe.components.song

import android.content.Context
import android.util.Log
import com.example.vivibe.MainViewModel
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.model.DownloadedSong
import com.example.vivibe.model.PlaySong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class DownloadManager(private val context: Context, private val dbHelper: DatabaseHelper, private val songClient: SongClient) {
    private val client = OkHttpClient()

    suspend fun downloadSong(googleId: String, songId: Int, onProgress: (Float) -> Unit): Result<DownloadedSong> {
        try {
            val song = songClient.fetchDownloadedSong(songId)
                ?: return Result.failure(Exception("Song not found"))

            return try {
                // Create directories if they don't exist
                val audioDir = File(context.filesDir, "audio").apply { mkdirs() }
                val imageDir = File(context.filesDir, "images").apply { mkdirs() }

                // Generate unique filenames
                val audioFileName = "audio_${song.id}.mp3"
                val imageFileName = "thumb_${song.id}.jpg"

                // Download files
                val audioFile = File(audioDir, audioFileName)
                val imageFile = File(imageDir, imageFileName)

                coroutineScope {
                    // Download audio and image in parallel, track audio progress
                    val audioJob = async {
                        var lastProgress = 0f
                        downloadFile(song.audio, audioFile) { progress ->
                            // Only update if progress increased by at least 1%
                            if (progress - lastProgress > 0.01f) {
                                lastProgress = progress
                                // Update progress in ViewModel
                                onProgress(progress)
                            }
                        }
                    }
                    val imageJob = async {
                        downloadFile(song.thumbnailUrl, imageFile) { _ -> }
                    }

                    // Wait for both downloads to complete
                    audioJob.await()
                    imageJob.await()
                }

                val downloadedSong = DownloadedSong(
                    id = song.id,
                    title = song.title,
                    thumbnailPath = imageFile.absolutePath,
                    artistId = song.artist.id,
                    artistName = song.artist.name,
                    audioPath = audioFile.absolutePath,
                    duration = song.duration,
                    lyrics = song.lyrics,
                    views = song.views,
                    likes = song.likes,
                    dominantColor = song.dominantColor
                )

                Log.d("DownloadManager", "Attempting to save song: $songId")
                // Save to database
                if (dbHelper.insertDownloadedSong(googleId, downloadedSong)) {
                    Log.d("DownloadManager", "Successfully saved song: $songId")
                    return Result.success(downloadedSong)
                } else {
                    // Clean up files if database insert fails
                    audioFile.delete()
                    imageFile.delete()
                    Log.e("DownloadManager", "Failed to save song to database: $songId")
                    return Result.failure(Exception("Failed to save song to database"))
                }
            } catch (e: Exception) {
                Log.e("DownloadManager", "Error downloading song: ${e.message}")
                return Result.failure(e)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun downloadFile(url: String, destination: File, onProgress: (Float) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use {response ->
                    if(!response.isSuccessful) throw IOException("Failed to download: $url")

                    response.body?.let { body ->
                        val contentLength = body.contentLength()
                        var bytesWritten = 0L
                        destination.outputStream().use { output ->
                            body.byteStream().use { input ->
                                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                var bytes = input.read(buffer)

                                while (bytes >= 0) {
                                    output.write(buffer, 0, bytes)
                                    bytesWritten += bytes

                                    // Calculate and report progress
                                    if (contentLength > 0) {
                                        val progress = bytesWritten.toFloat() / contentLength.toFloat()
                                        onProgress(progress)
                                    }

                                    bytes = input.read(buffer)
                                }
                            }
                        }
                    } ?: throw IOException("Empty response body")
                }
            } catch (e: Exception) {
                destination.delete()
                throw e
            }
        }
    }

    private fun getDownloadedSong(googleId: String, songId: Int): DownloadedSong? {
        return dbHelper.getDownloadedSong(googleId, songId)
    }

    fun getAllDownloadedSongs(googleId: String): List<DownloadedSong> {
        return dbHelper.getAllDownloadedSongs(googleId)
    }

    fun deleteDownloadedSong(googleId: String, songId: Int): Boolean {
        val song = getDownloadedSong(googleId, songId)?: return false
        return try {
            File(song.audioPath).delete()
            File(song.thumbnailPath).delete()
            dbHelper.deleteDownloadedSong(googleId, songId)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}