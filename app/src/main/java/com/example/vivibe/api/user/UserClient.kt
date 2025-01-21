package com.example.vivibe.api.user

import android.content.Context
import android.util.Log
import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.PlaylistReview
import com.example.vivibe.model.SongDetail
import com.example.vivibe.model.SongHistory

class UserClient (context: Context, token: String?) {
    private val tag = "UserClient"

    private val userService = token?.let { UserService(context, it) }


    suspend fun toggleLike(userId: String, songId: Int): Boolean {
        Log.d(tag, "toggleLike called - userId: $userId, songId: $songId")

        if(userService == null) {
            Log.e(tag, "UserService is null")
            return false
        }

        return try {
            Log.d(tag, "Calling likeService.toggleLike") // Thêm log
            val response = userService.toggleLike(userId, songId.toString())
            Log.d(tag, "Response received: $response") // Thêm log

            if(response?.err == 0) {
                Log.d(tag, "Toggle like success - userId: $userId, songId: $songId")
                response.liked
            } else {
                Log.e(tag, "Toggle like failed with error: ${response?.msg}")
                false
            }
        } catch (e: Exception) {
            Log.e(tag, "Toggle like failed", e)
            false
        }
    }

    suspend fun getLikeStatus(userId: String, songId: Int): Boolean {
        if(userService == null) {
            Log.e(tag, "LikeService is null")
            return false
        }
        return try {
            val response = userService.getLikeStatus(userId, songId.toString())
            if(response?.err == 0) {
                Log.d(tag, "Get like status success - userId: $userId, songId: $songId")
                response.liked
            } else {
                Log.e(tag, "Get like status failed with error: ${response?.msg}")
                false
            }
        } catch (e: Exception) {
            Log.e(tag, "Get like status failed", e)
            false
        }
    }

    suspend fun toggleFollow(userId: String, artistId: Int): Boolean {
        Log.d(tag, "toggleFollow called - userId: $userId, artistId: $artistId")

        if(userService == null) {
            Log.e(tag, "UserService is null")
            return false
        }

        return try {
            Log.d(tag, "Calling userService.toggleFollow") // Thêm log
            val response = userService.toggleFollow(userId, artistId.toString())
            Log.d(tag, "Response received: $response") // Thêm log

            if(response?.err == 0) {
                Log.d(tag, "Toggle follow success - userId: $userId, artistId: $artistId")
                response.followed
            } else {
                Log.e(tag, "Toggle follow failed with error: ${response?.msg}")
                false
            }
        } catch (e: Exception) {
            Log.e(tag, "Toggle follow failed", e)
            false
        }
    }

    suspend fun getFollowStatus(userId: String, artistId: Int): Boolean {
        if(userService == null) {
            Log.e(tag, "UserService is null")
            return false
        }
        return try {
            val response = userService.getFollowStatus(userId, artistId.toString())
            if(response?.err == 0) {
                Log.d(tag, "Get follow status success - userId: $userId, artistId: $artistId")
                response.followed
            } else {
                Log.e(tag, "Get follow status failed with error: ${response?.msg}")
                false
            }
        } catch (e: Exception) {
            Log.e(tag, "Get follow status failed", e)
            false
        }
    }

    suspend fun getPlaylists(userId: String): List<PlaylistReview> {
        if(userService == null) {
            Log.e(tag, "UserService is null")
            return emptyList()
        }
        return try {
            val response = userService.getPlaylists(userId)
            if(response?.err == 0) {
                response.playlists.also {
                    Log.d(tag, "Get playlists success - userId: $userId")
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(tag, "Get playlists failed", e)
            emptyList()
        }
    }

    suspend fun getLikedArtists(userId: String): List<ArtistDetail> {
        if(userService == null) {
            Log.e(tag, "UserService is null")
            return emptyList()
        }

        return try {
            val response = userService.getLikedArtists(userId)
            if(response?.err == 0) {
                response.artists.also {
                    Log.d(tag, "Get liked artists success - userId: $userId")
                } ?: emptyList()
            } else {
                emptyList<ArtistDetail>().also {
                    Log.e(tag, "Get liked artists failed with error: ${response?.msg}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(tag, "Error getting liked artists")
            emptyList()
        }
    }

    suspend fun search(keyword: String): List<SongDetail> {
        if(userService == null) {
            Log.e(tag, "UserService is null")
            return emptyList()
        }

        return try {
            val response = userService.search(keyword)
            if(response?.err == 0) {
                response.songs.also {
                    Log.d(tag, "Search success - keyword: $keyword")
                } ?: emptyList()
            } else {
                emptyList<SongDetail>().also {
                    Log.e(tag, "Search failed with error: ${response?.msg}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(tag, "Search failed", e)
            emptyList()
        }
    }

    suspend fun getHistories(userId: String): List<SongHistory> {
        if(userService == null) {
            Log.e(tag, "UserService is null")
            return emptyList()
        }

        return try {
            val response = userService.getHistories(userId)
            if(response?.err == 0) {
                response.histories.also {
                    Log.d(tag, "Get histories success - userId: $userId")
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateHistory(userId: String, songId: Int): Boolean {
        if(userService == null) {
            Log.e(tag, "UserService is null")
            return false
        }

        return try {
            val response = userService.updateHistory(userId, songId)
            if(response?.err == 0) {
                Log.d(tag, "Update history success - userId: $userId, songId: $songId")
                true
            } else {
                Log.e(tag, "Update history failed with error: ${response?.msg}")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(tag, "Update history failed", e)
            false
        }
    }

    suspend fun upgradeToPremium(userId: String): Int {
        if(userService == null) {
            Log.e(tag, "UserService is null")
            return 0
        }
        return try {
            val response = userService.upgradeToPremium(userId)
            if(response?.err == 0) {
                Log.d(tag, "Upgrade to premium success - userId: $userId")
                response.premium
            } else {
                Log.e(tag, "Upgrade to premium failed with error: ${response?.msg}")
                0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(tag, "Upgrade to premium failed", e)
            0
        }
    }
}