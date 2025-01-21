package com.example.vivibe.api.common

import android.content.Context
import android.util.Log
import com.example.vivibe.model.AlbumReview
import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.ArtistReview
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SpeedDialSong

class CommonClient(context: Context, token: String?) {
    private val tag = "CommonClient"

    private val commonService = token?.let { CommonService(context, it) }

    suspend fun getLatestAlbum() : AlbumReview? {
        return try {
            val response = commonService?.getLatestAlbum()
            Log.d(tag, "Latest album response: $response")
            if(response?.err == 0) {
                response.data.also {
                    Log.d(tag, "Album thumbnails: ${it.thumbnails}")
                    println("$tag New release fetched successfully")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching latest album", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun getAlbumsAndSongs(): AlbumsSongs? {
        return try {
            val response = commonService?.getAlbumsAndSongs()
            if(response?.err == 0) {
                AlbumsSongs(response.albums, response.artists)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchTopSongs(): List<QuickPicksSong> {
        if(commonService == null) return emptyList()
        return try {
            val response = commonService.fetchTopSongs()
            if(response?.err == 0) {
                response.data.also {
                    Log.d(tag, "Top songs fetched successfully")
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun fetchTopArtists(): List<ArtistDetail> {
        if(commonService == null) return emptyList()
        return try {
            val response = commonService.fetchTopArtists()
            if(response?.err == 0) {
                response.data.also {
                    Log.d(tag, "Top songs fetched successfully")
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

data class AlbumsSongs(
    val albums: List<AlbumReview>,
    val artists: List<ArtistReview>
)