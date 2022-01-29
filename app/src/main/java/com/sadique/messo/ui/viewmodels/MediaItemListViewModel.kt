package com.sadique.messo.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.sadique.messo.models.MediaItemData
import com.sadique.messo.utils.Constants
import com.sadique.musicservice.common.EMPTY_PLAYBACK_STATE
import com.sadique.musicservice.common.MusicServiceConnection
import com.sadique.musicservice.common.NOTHING_PLAYING
import com.sadique.musicservice.media.extensions.id
import com.sadique.musicservice.media.extensions.isPlaying
import com.sadique.musicservice.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaItemListViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mediaId = savedStateHandle.get<String>(Constants.ARG_ROOT_ID)!!

    /**
     * Use a backing property so consumers of mediaItems only get a [LiveData] instance so
     * they don't inadvertently modify it.
     */
    private val _mediaItems = MutableLiveData<Resource<List<MediaItemData>>>()
        .apply { postValue(Resource.loading(null)) }
    val mediaItems: LiveData<Resource<List<MediaItemData>>> get() = _mediaItems

    /**
     * Pass the status of the [MusicServiceConnection.networkFailure] through.
     */
    val networkError = Transformations.map(musicServiceConnection.networkError) { it }

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
        ) {
            val itemsList = children.map { child ->
                child.toMediaItemData()
            }
            _mediaItems.postValue(Resource.success(itemsList))
        }

        override fun onError(parentId: String) {
            _mediaItems.postValue(Resource.error(
                "Something went wrong",
                _mediaItems.value?.data
            ))
        }
    }

    fun MediaBrowserCompat.MediaItem.toMediaItemData(): MediaItemData {
        val subtitle = description.subtitle ?: ""
        return MediaItemData(
            mediaId!!,
            description.title.toString(),
            subtitle.toString(),
            description.iconUri,
            description.iconBitmap,
            null,
            isBrowsable,
            isPlaying = musicServiceConnection.playbackState.value?.isPlaying ?: false
        )
    }

    /**
     * When the session's [PlaybackStateCompat] changes, the [mediaItems] need to be updated
     * so the correct [MediaItemData.playbackRes] is displayed on the active item.
     * (i.e.: play/pause button or blank)
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        val playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = musicServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        if (metadata.id != null) {
            _mediaItems.postValue(Resource.success(updateState(playbackState, metadata)))
        }
    }

    /**
     * When the session's [MediaMetadataCompat] changes, the [mediaItems] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their [MediaItemData.playbackRes]
     * changed. (i.e.: play/pause button or blank)
     */
    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        val playbackState = musicServiceConnection.playbackState.value ?: EMPTY_PLAYBACK_STATE
        val metadata = it ?: NOTHING_PLAYING
        if (metadata.id != null) {
            _mediaItems.postValue(Resource.success(updateState(playbackState, metadata)))
        }
    }

    /**
     * Because there's a complex dance between this [ViewModel] and the [MusicServiceConnection]
     * (which is wrapping a [MediaBrowserCompat] object), the usual guidance of using
     * [Transformations] doesn't quite work.
     *
     * Specifically there's three things that are watched that will cause the single piece of
     * [LiveData] exposed from this class to be updated.
     *
     * [subscriptionCallback] (defined above) is called if/when the children of this
     * ViewModel's [mediaId] changes.
     *
     * [MusicServiceConnection.playbackState] changes state based on the playback state of
     * the player, which can change the [MediaItemData.playbackRes]s in the list.
     *
     * [MusicServiceConnection.nowPlaying] changes based on the item that's being played,
     * which can also change the [MediaItemData.playbackRes]s in the list.
     */
    private val musicServiceConnection = musicServiceConnection.also {
        it.subscribe(mediaId, subscriptionCallback)

        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat,
    ): List<MediaItemData> {

        return mediaItems.value?.data?.map {
            if (it.mediaId == mediaMetadata.id) it.isPlaying = playbackState.isPlaying
            it
        } ?: emptyList()
    }

    /**
     * Since we use [LiveData.observeForever] above (in [musicServiceConnection]), we want
     * to call [LiveData.removeObserver] here to prevent leaking resources when the [ViewModel]
     * is not longer in use.
     *
     * For more details, see the kdoc on [musicServiceConnection] above.
     */
    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers from the MusicServiceConnection.
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)

        // And then, finally, unsubscribe the media ID that was being watched.
        musicServiceConnection.unsubscribe(mediaId, subscriptionCallback)
    }

}

private const val TAG = "MediaItemListFragmentVM"
private const val NO_RES = 0
