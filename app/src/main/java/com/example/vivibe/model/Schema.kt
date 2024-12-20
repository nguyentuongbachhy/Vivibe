package com.example.vivibe.model

data class User(
    val googleId: String?,
    val name: String?,
    val email: String?,
    val profilePictureUri: String?
)

data class Artist(
    val id: Int,
    val name: String
)

data class SpeedDialSong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
)

data class PlaySong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val artist: Artist,
    val audio: String,
    val lyrics: String,
    val duration: Int,
    val views: Int,
    val likes: Int
)

data class QuickPicksSong(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val views: Int,
    val artist: Artist
)

data class Genre(
    val id: Int,
    val name: String
)