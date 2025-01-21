package com.example.vivibe.api.common

import android.content.Context
import com.example.vivibe.R
import com.example.vivibe.api.RetrofitClient

class CommonService(context: Context, token: String) {
    private val api: CommonInterface
    private val tag = "CommonService"

    init {
        val baseURL:String = context.getString(R.string.BASE_URL)
        val retrofit = RetrofitClient.getClient(baseURL, token)
        api = retrofit.create(CommonInterface::class.java)
    }

    suspend fun getLatestAlbum(): LatestReleaseResponse? {
        return try {
            val response = api.getLatestAlbum()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAlbumsAndSongs(): AlbumsSongsResponse? {
        return try {
            val response = api.getAlbumsAndSongs()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchTopSongs(): TopSongsResponse? {
        return try {
            val response = api.fetchTopSongs()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchTopArtists(): TopArtistsResponse? {
        return try {
            val response = api.fetchTopArtists()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}