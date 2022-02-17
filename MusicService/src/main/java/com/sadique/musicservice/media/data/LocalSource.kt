package com.sadique.musicservice.media.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.sadique.musicservice.media.extensions.*
import com.sadique.musicservice.media.library.AbstractMusicSource
import com.sadique.musicservice.media.library.STATE_ERROR
import com.sadique.musicservice.media.library.STATE_INITIALIZED
import com.sadique.musicservice.media.library.STATE_INITIALIZING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

class LocalSource(private val context: Context) : AbstractMusicSource() {

    companion object {
        const val CURSOR_START_INDEX = 0
    }

    private var _catalog: MutableList<MediaMetadataCompat> = mutableListOf()

    init {
        state = STATE_INITIALIZING
    }

    override fun getCatalog(): MutableList<MediaMetadataCompat> = _catalog

    override fun iterator(): Iterator<MediaMetadataCompat> = _catalog.iterator()

    override suspend fun load() {
        updateCatalog()?.let { updatedCatalog ->
            _catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            _catalog = mutableListOf()
            state = STATE_ERROR
        }
    }


    private suspend fun updateCatalog(): MutableList<MediaMetadataCompat>? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                loadFromStorage()
            } catch (ioException: IOException) {
                Timber.e(ioException)
                null
            }
        }
    }

    private val uri: Uri
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        }

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.DATA // to get the file path
    )


    private fun getCursor(query: String?): Cursor? {
        val selection = if (query == null || query.trim() == "") null
            else "${MediaStore.Audio.Media.TITLE} LIKE '%$query%'"
        return context.contentResolver.query(
            uri, projection, selection, null, null
        )
    }

    private fun loadFromStorage(
        query: String? = null,
        index: Int? = null,
        loadSize: Int? = null,
    ): MutableList<MediaMetadataCompat>? {
        var musicFiles: MutableList<MediaMetadataCompat>? = null
        getCursor(query)?.let {
            musicFiles = processCursor(it, index ?: CURSOR_START_INDEX, loadSize)
            it.close()
        }
        return musicFiles
    }

    private fun processCursor(
        cursor: Cursor,
        index: Int,
        loadSize: Int?,
    ): MutableList<MediaMetadataCompat> =
        with(cursor) {
            val musicFiles = mutableListOf<MediaMetadataCompat>()

            if (index >= count || count <= 0) return@with musicFiles

            moveToPosition(index)

            val columnId = getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val columnData = getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val columnTitle = getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val columnAlbum = getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val columnArtist = getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val columnTrack = getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            val retriever = MediaMetadataRetriever()

            do {
                val mediaMetadataCompat = try {
                    val mediaId: Long = getLong(columnId)
                    val path: String = getString(columnData) // gives the file path
                    val mediaTitle: String = getString(columnTitle)
                    val mediaAlbum: String = getString(columnAlbum)
                    val mediaArtist: String = getString(columnArtist)
                    val track: Int = getInt(columnTrack)
                    val uri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )

                    retriever.setDataSource(path)

                    MediaMetadataCompat.Builder().apply {

                        val art = retriever.embeddedPicture?.toBitmap()

                        id = mediaId.toString()
                        title = mediaTitle
                        artist = mediaArtist
                        album = mediaAlbum
                        mediaUri = uri.toString()
                        trackNumber = track.toLong()
                        albumArt = art
                        genre = retriever.genre
                        flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

                        // To make things easier for *displaying* these, set the display properties as well.
                        displayTitle = mediaTitle
                        displaySubtitle = mediaArtist
                        displayDescription = mediaAlbum
                        displayIcon = art

                        // Add downloadStatus to force the creation of an "extras" bundle in the resulting
                        // MediaMetadataCompat object. This is needed to send accurate metadata to the
                        // media session during updates.
                        this.downloadStatus = MediaDescriptionCompat.STATUS_DOWNLOADED
                    }.build().apply {
                        description.extras?.putAll(bundle)
                    }

                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }

                mediaMetadataCompat?.let { musicFiles.add(it) }

            } while (moveToNext() && (loadSize == null || position < (index + loadSize)))

            retriever.release()
            return@with musicFiles
        }


}