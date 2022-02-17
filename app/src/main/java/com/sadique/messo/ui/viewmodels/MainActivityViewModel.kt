/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sadique.messo.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sadique.messo.models.MediaItemData
import com.sadique.messo.utils.Constants.MEDIA_ROOT_ID
import com.sadique.musicservice.common.MusicServiceConnection
import com.sadique.musicservice.media.extensions.id
import com.sadique.musicservice.media.extensions.isPlayEnabled
import com.sadique.musicservice.media.extensions.isPlaying
import com.sadique.musicservice.media.extensions.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Small [ViewModel] that watches a [MusicServiceConnection] to become connected
 * and provides the root/initial media ID of the underlying [MediaBrowserCompat].
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection,
) : ViewModel() {

    val rootMediaId: LiveData<String> =
        Transformations.map(musicServiceConnection.isConnected) { isConnected ->
            if (isConnected.peekContent().data == true) {
                musicServiceConnection.rootMediaId
            } else {
                null
            }
        }

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val nowPlaying = musicServiceConnection.nowPlaying
    val playbackState = musicServiceConnection.playbackState

    private val transportControls: MediaControllerCompat.TransportControls
        get() = musicServiceConnection.mediaController.transportControls

    private val musicServiceConnection = musicServiceConnection.also {
        it.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    fun skipToNextSong() {
        transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        transportControls.seekTo(pos)
    }

    fun shuffleAll() {
        transportControls.setShuffleMode(SHUFFLE_MODE_ALL)
    }

    fun shuffleNone() {
        transportControls.setShuffleMode(SHUFFLE_MODE_NONE)
    }

    fun repeatAll() {
        transportControls.setRepeatMode(REPEAT_MODE_ALL)
    }

    fun repeatOne() {
        transportControls.setRepeatMode(REPEAT_MODE_ONE)
    }

    fun repeatNone() {
        transportControls.setRepeatMode(REPEAT_MODE_NONE)
    }

    fun playOrToggleMedia(mediaItem: MediaItemData, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == nowPlaying.value?.id) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> Unit
                }
            }
        } else {
            transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

}

