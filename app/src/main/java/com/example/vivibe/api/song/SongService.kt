package com.example.vivibe.api.song

import android.content.Context
import android.util.Log
import com.example.vivibe.R
import com.example.vivibe.api.RetrofitClient
import com.example.vivibe.model.FullInfoPlaylist


class SongService(context: Context, token: String) {
    private val api: SongInterface
    private val tag = "SongService"

    init {
        val baseURL:String = context.getString(R.string.BASE_URL)
        val retrofit = RetrofitClient.getClient(baseURL, token)
        api = retrofit.create(SongInterface::class.java)
    }

    suspend fun fetchSpeedDialSongs(): SpeedDialResponse? {
        return try {
            val response = api.fetchSpeedDialSongs()
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

    suspend fun fetchQuickPickSongs(songIds: List<Int>) : QuickPicksResponse? {
        return try {
            val response = api.fetchQuickPickSongs(QuickPicksRequest(songIds))
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

    suspend fun fetchAlbums(artistIds: List<Int>) : ArtistAlbumResponse? {
        return try {
            val response = api.fetchAlbums(ArtistAlbumRequest(artistIds))
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchNewRelease(): QuickPicksResponse? {
        return try {
            val response = api.fetchNewRelease()
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchArtistAndAlbum(artistId: Int): FullInfoArtistResponse? {
        return try {
            val response = api.fetchArtistAndAlbum(artistId)
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchPlayAll(songIds: List<Int>) : PlayingSongResponse? {
        return try {
            val response = api.fetchPlayAll(QuickPicksRequest(songIds))
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e:Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchSongsByGenre(genreId: Int) : GenreSongsResponse? {
        return try {
            val response = api.fetchSongsByGenre(genreId)
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchDetailAlbum(albumId: Int): FullInfoAlbumResponse? {
        return try {
            val response = api.fetchDetailAlbum(albumId)
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchDetailPlaylist(playlistId: Int): FullInfoPlaylistResponse? {
        return try {
            val response = api.fetchDetailPlaylist(playlistId)
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getLikedSongs(userId: String): LikedSongsResponse? {
        return try {
            val response = api.getLikedSongs(userId)
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchPlayingSong(songId: Int) : PlayingSongResponse? {
        return try {
            val response = api.fetchPlayingSong(songId)
            if (response.isSuccessful) {
                Log.d(tag, "fetched playing song successfully")
                response.body()
            } else {
                null
            }
        }catch (e:Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchDownloadedSong(songId: Int): DownloadedSongResponse? {
        return try {
            val response = api.fetchDownloadedSong(songId)
            if(response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchNameAndSongs(genreId: Int): NameAndSongsResponse? {
        return try {
            val response = api.fetchNameAndSongs(genreId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e:Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchSwipeSongs(genreIds: List<Int>): SwipeSongsResponse? {
        return try {
            val response = api.fetchSwipeSongs(SwipeSongsRequest(genreIds))
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
}