package com.example.vivibe.model

data class User(
    val token: String? = null,
    val googleId: String? = null,
    val name: String = "",
    val email: String = "",
    val profilePictureUri: String = "",
    val premium: Int = 0
) {
    fun isValid(): Boolean = !token.isNullOrBlank() && !googleId.isNullOrBlank()
}

data class ArtistReview(
    val id: Int,
    val name: String,
    val thumbnail: String
)

data class ArtistDetail(
    val id: Int,
    val name: String,
    val thumbnail: String,
    val description: String,
    val followers: Int
)

data class FullInfoArtist(
    val title: String,
    val likes: Int,
    val createdAt: String,
    val artist: ArtistDetail,
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

data class QuickPicksSong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val views: Long,
    val artist: ArtistReview
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
    val views: Long
)

data class GenreSong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val duration: Int,
    val views: Long,
    val artist: ArtistReview
)

data class GenreSongs(
    val genreId: Int,
    val genreName: String,
    val songs: List<GenreSong>
)

data class ArtistAlbum(
    val title: String,
    val artist: ArtistReview,
    val songs: List<SongReview>
)

data class Genre(
    val id: Int,
    val name: String
) {
    companion object {
        val ALL: Genre = Genre(0, "All")
    }
}