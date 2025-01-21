package com.example.vivibe.api.song

import android.content.Context
import android.util.Log
import com.example.vivibe.model.ArtistAlbum
import com.example.vivibe.model.FullInfoAlbum
import com.example.vivibe.model.FullInfoArtist
import com.example.vivibe.model.FullInfoPlaylist
import com.example.vivibe.model.GenreSongs
import com.example.vivibe.model.NameAndSongs
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SongDetail
import com.example.vivibe.model.SpeedDialSong
import com.example.vivibe.model.SwipeSong

class SongClient(context: Context, token: String?) {
    private val tag = "SongClient: "

    private val songService = token?.let { SongService(context, it) }

    suspend fun fetchSpeedDialSongs(): List<SpeedDialSong> {
        if(songService == null) return emptyList()
        return try {
            val response = songService.fetchSpeedDialSongs()
            if (response?.err == 0) {
                response.data?.also {
                    println("$tag Songs fetched successfully")
                } ?: emptyList()
            } else {
                println("$tag Error: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            println("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchSongs(songIds: List<Int>): List<QuickPicksSong> {
        if(songService == null) return emptyList()
        return try {
            val response = songService.fetchQuickPickSongs(songIds)
            if (response?.err == 0) {
                response.data?.also {
                    println("$tag Songs fetched successfully")
                } ?: emptyList()
            } else {
                print("$tag Error: ${response?.msg}")
                emptyList()
            }
        }catch (e: Exception) {
            print("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchAlbums(artistIds: List<Int>): List<ArtistAlbum> {
        if(songService == null) return emptyList()
        return try {
            val response = songService.fetchAlbums(artistIds)
            if(response?.err == 0) {
                println(response.data)
                response.data.also {
                    println("$tag Albums fetched successfully")
                }
            } else {
                print("$tag Error: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            print("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchArtistAndAlbum(artistId: Int): FullInfoArtist? {
        if(songService == null) return null
        return try {
            val response = songService.fetchArtistAndAlbum(artistId)
            if(response?.err == 0) {
                response.data.also {
                    println("$tag Artist and album fetched successfully")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            print("$tag Error: ${e.message}")
            null
        }
    }

    suspend fun fetchDetailAlbum(albumId: Int): FullInfoAlbum? {
        if(songService == null) return null
        return try {
            val response = songService.fetchDetailAlbum(albumId)
            if(response?.err == 0) {
                response.data.also {
                    println("$tag Artist and album fetched successfully")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            print("$tag Error: ${e.message}")
            null
        }
    }

    suspend fun fetchDetailPlaylist(playlistId: Int): FullInfoPlaylist? {
        if(songService == null) return null
        return try {
            val response = songService.fetchDetailPlaylist(playlistId)
            if(response?.err == 0) {
                response.data.also {
                    println("$tag Detail playlist fetched successfully")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            print("$tag Error: ${e.message}")
            null
        }
    }

    suspend fun getLikedSongs(userId: String) : List<SongDetail> {
        if(songService == null) return emptyList()

        return try {
            val response = songService.getLikedSongs(userId)
            if(response?.err == 0) {
                response.data.also {
                    println("$tag Liked songs fetched successfully")
                } ?: emptyList()
            } else {
                print("$tag Error: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(tag, "Error: ${e.message}")
            emptyList()
        }
    }


    suspend fun fetchNewRelease(): List<QuickPicksSong> {
        if(songService == null) return emptyList()
        return try {
            val response = songService.fetchNewRelease()
            println("Fetched new releases response: $response")
            if (response?.err == 0) {
                response.data?.also {
                    println("$tag New releases fetched successfully")
                } ?: emptyList()
            } else {
                println("$tag Error: $response")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun fetchPlayAll(songIds: List<Int>): List<PlaySong> {
        if(songService == null) return emptyList()
        return try {
            val response = songService.fetchPlayAll(songIds)
            if(response?.err == 0) {
                response.data?.also {
                    println("$tag Play all fetched successfully")
                } ?: emptyList()
            } else {
                print("$tag Error: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            print("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchNameAndSongs(genreId: Int): NameAndSongs? {
        if(songService == null) return null

        return try {
            val response = songService.fetchNameAndSongs(genreId)
            if(response?.err == 0) {
                response.data?.also {
                    println("$tag Name and songs fetched successfully")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchSongsByGenre(genreId: Int): List<GenreSongs> {
        return try {
            if(songService == null) return emptyList()
            val response = songService.fetchSongsByGenre(genreId)
            println("Fetched songs by genre response: $response")
            if(response?.err == 0) {
                response.data?.also {
                    println("$tag Songs by genre fetched successfully")
                } ?: emptyList()
            } else {
                print("$tag Error: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            print("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchPlayingSong(songId: Int) : List<PlaySong> {
        if(songService == null) return emptyList()
        return try {
            val response = songService.fetchPlayingSong(songId)
            if (response?.err == 0) {
                response.data?.also {
                    println("$tag Song fetched successfully")
                } ?: emptyList()
            } else {
                print("$tag Error: ${response?.msg}")
                emptyList()
            }
        }catch (e: Exception) {
            print("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchDownloadedSong(songId: Int): PlaySong? {
        if(songService == null) return null
        return try {
            val response = songService.fetchDownloadedSong(songId)
            if(response?.err == 0) {
                response.data?.also {
                    println("$tag Downloaded song fetched successfully")
                }
            } else {
                print("$tag Error: ${response?.msg}")
                null
            }
        } catch (e: Exception) {
            print("$tag Error: ${e.message}")
            null
        }
    }

    suspend fun fetchSwipeSongs(genreIds: List<Int>): List<SwipeSong> {
        if(songService == null) {
            Log.e(tag, "Song service is null")
            return emptyList()
        }
        return try {
            Log.d(tag, "Fetching swipe songs with genre IDs: $genreIds")
            val response = songService.fetchSwipeSongs(genreIds)
            Log.d(tag, "Swipe songs response: $response")

            if(response?.err == 0) {
                val songs = response.data?.also {
                    Log.d(tag, "Swipe songs fetched successfully. Count: ${it.size}")
                    it.forEach { song ->
                        Log.d(tag, "Song details: ${song.title}, Audio: ${song.audio}")
                    }
                } ?: emptyList()
                songs
            } else {
                Log.e(tag, "Error getting swipe songs: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception in fetchSwipeSongs", e)
            emptyList()
        }
    }
}