package com.example.vivibe.api.user

import android.content.Context
import android.util.Log
import com.example.vivibe.R
import com.example.vivibe.api.RetrofitClient

class UserService(context: Context, token: String) {
    private val api: UserInterface
    private val tag = "UserService"
    init {
        val baseURL:String = context.getString(R.string.BASE_URL)
        val retrofit = RetrofitClient.getClient(baseURL, token)
        api = retrofit.create(UserInterface::class.java)
    }

    suspend fun toggleLike(userId: String, songId: String): LikeResponse? {
        Log.d(tag, "toggleLike service called - userId: $userId, songId: $songId") // Thêm log
        return try {
            Log.d(tag, "Making API call") // Thêm log
            val response = api.toggleLike(ToggleLikeRequest(userId, songId))
            Log.d(tag, "API response received: ${response.isSuccessful}") // Thêm log

            if (response.isSuccessful) {
                Log.d(tag, "Response body: ${response.body()}") // Thêm log
                response.body()
            } else {
                Log.e(tag, "Toggle like failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Toggle like failed", e)
            null
        }
    }

    suspend fun getLikeStatus(userId: String, songId: String): LikeResponse? {
        return try {
            val response = api.getLikeStatus(userId, songId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(tag, "Get like status failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Get like status failed", e)
            null
        }
    }

    suspend fun toggleFollow(userId: String, artistId: String): FollowResponse? {
        Log.d(tag, "toggleFollow service called - userId: $userId, artistId: $artistId") // Thêm log
        return try {
            Log.d(tag, "Making API call") // Thêm log
            val response = api.toggleFollow(ToggleFollowRequest(userId, artistId))
            Log.d(tag, "API response received: ${response.isSuccessful}") // Thêm log

            if (response.isSuccessful) {
                Log.d(tag, "Response body: ${response.body()}") // Thêm log
                response.body()
            } else {
                Log.e(tag, "Toggle follow failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Toggle follow failed", e)
            null
        }
    }

    suspend fun getFollowStatus(userId: String, artistId: String): FollowResponse? {
        return try {
            val response = api.getFollowStatus(userId, artistId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(tag, "Get follow status failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Get follow status failed", e)
            null
        }
    }

    suspend fun getPlaylists(userId: String): PlaylistsResponse? {
        return try {
            val response = api.getPlaylists(userId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(tag, "Get playlists failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Get playlists failed", e)
            null
        }
    }

    suspend fun search(keyword: String): SearchResponse? {
        return try {
            val response = api.search(keyword)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Search failed", e)
            null
        }
    }

    suspend fun getHistories(userId: String): HistoriesResponse? {
        return try {
            val response = api.getHistories(userId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(tag, "Get histories failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Get histories failed", e)
            null
        }
    }

    suspend fun getLikedArtists(userId: String): LikedArtistsResponse? {
        return try {
            val response = api.getLikedArtists(userId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(tag, "Get liked artists failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Get liked artists failed", e)
            null
        }
    }

    suspend fun upgradeToPremium(userId: String): UpgradeResponse? {
        return try {
            val response = api.upgradeToPremium(UpgradeRequest(userId))
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(tag, "Upgrade to premium failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(tag, "Upgrade to premium failed", e)
            null
        }
    }

    suspend fun updateHistory(userId: String, songId: Int): UpdateHistoryResponse? {
        return try {
            val response = api.updateHistory(UpdateHistoryRequest(userId, songId))
            if(response.isSuccessful) {
                response.body()
            } else {
                Log.e(tag, "Update history failed with code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(tag, "Update history failed", e)
            null
        }
    }
}