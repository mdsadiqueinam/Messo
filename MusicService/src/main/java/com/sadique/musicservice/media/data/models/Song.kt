package com.sadique.musicservice.media.data.models

data class Song(
    val mediaId: String = "",
    val title: String = "",
    val mediaUri: String = "",
    val albumArtUri: String = "",
    val album: String = "",
    val artist: String = "",
    val genre: String = "",
    val trackNumber: Long = 0,
    val totalTrackCount: Long = 0
)
