package com.sadique.musicservice.media.extensions

import android.media.MediaMetadataRetriever
import timber.log.Timber

inline val MediaMetadataRetriever.genre: String?
    get() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)