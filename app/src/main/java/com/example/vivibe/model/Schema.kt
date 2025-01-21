package com.example.vivibe.model

import java.util.Date

data class User(
    val id: String? = null,
    val token: String? = null,
    val googleId: String? = null,
    val name: String = "",
    val email: String = "",
    val profilePictureUri: String = "",
    val premium: Int = 0
) {
    fun isValid(): Boolean = !token.isNullOrBlank() && !googleId.isNullOrBlank()
}

data class CommentItem(
    val id: Int,
    val songId: Int,
    val content: String,
    val depth: Int,
    val likes: Int,
    val countReplies: Int,
    val createdAt: Date,
    val user: User
)

data class ArtistReview(
    val id: Int,
    val name: String,
    val thumbnail: String
)

data class ArtistDetail(
    val id: Int,
    val name: String,
    val thumbnail: String,
    val description: String = "",
    val followers: Int
)

data class FullInfoArtist(
    val title: String,
    val likes: Int,
    val createdAt: String,
    val artist: ArtistDetail,
    val songs: List<SongDetail>
)

data class FullInfoAlbum(
    val title: String,
    val likes: Int,
    val createdAt: String,
    val songs: List<SongDetail>
)

data class FullInfoPlaylist(
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: String,
    val songs: List<SongDetail>
)

data class SpeedDialSong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val artistId: Int
)

data class PlaySong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val artist: ArtistReview,
    val audio: String,
    val duration: Int,
    val lyrics: String,
    val views: Long,
    val likes: Int,
    val dominantColor: Int
)

data class SwipeSong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val artist: ArtistReview,
    val views: Long,
    val like: Int,
    val audio: String,
    val climaxStart: Int,
    val climaxEnd: Int
)

data class QuickPicksSong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val views: Long = 0L,
    val artist: ArtistReview
)

data class NameAndSongs(
    val name: String,
    val songs: List<QuickPicksSong>
)

data class SongReview(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
)

data class SongDetail(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val duration: Int,
    val views: Long,
    val lastPlayedAt: Date,
    val artist: ArtistReview? = null
)


data class SongHistory(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val duration: Int,
    val views: Long,
    val lastPlayedAt: Date,
    val artist: ArtistReview? = null
)

data class GenreSong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val duration: Int,
    val views: Long,
    val artist: ArtistReview
)

data class Genre(
    val id: Int,
    val name: String? = null
) {
    companion object {
        val ALL: Genre = Genre(0, "All")
    }
}

data class GenreSongs(
    val genre: Genre,
    val songs: List<GenreSong>
)

data class ArtistAlbum(
    val title: String,
    val artist: ArtistReview,
    val songs: List<SongReview>
)

data class AlbumReview(
    val id: Int,
    val title: String,
    val thumbnails: List<String>,
    val names: List<String>
)

data class PlaylistReview(
    val id: Int,
    val name: String,
    val userName: String,
    val thumbnails: List<String>
)

data class DownloadedSong(
    val id: Int,
    val title: String,
    val thumbnailPath: String,
    val artistId: Int,
    val artistName: String,
    val audioPath: String,
    val duration: Int,
    val lyrics: String,
    val views: Long,
    val likes: Int,
    val dominantColor: Int,
    val downloadDate: Long = System.currentTimeMillis()
)