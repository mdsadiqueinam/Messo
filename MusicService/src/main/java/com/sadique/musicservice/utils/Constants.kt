package com.sadique.musicservice.utils

object Constants {

    // BrowseTree
    const val MESSO_BROWSABLE_ROOT = "/"
    const val MESSO_EMPTY_ROOT = "@empty@"
    const val MESSO_RECOMMENDED_ROOT = "__RECOMMENDED__"
    const val MESSO_ALBUMS_ROOT = "__ALBUMS__"
    const val MESSO_RECENT_ROOT = "__RECENT__"
    const val MESSO_LOCAL_ROOT = "__LOCAL__"
    const val MESSO_REMOTE_ROOT = "__REMOTE__"

    const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

    const val RESOURCE_ROOT_URI = "android.resource://com.sadique.messo.next/drawable/"

    // The amount of time to wait for the album art file to download before timing out.
    const val DOWNLOAD_TIMEOUT_SECONDS = 30L

}