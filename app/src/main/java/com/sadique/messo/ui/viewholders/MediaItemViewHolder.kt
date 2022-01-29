package com.sadique.messo.ui.viewholders

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sadique.messo.R
import com.sadique.messo.databinding.MediaItemBinding
import com.sadique.messo.models.MediaItemData
import timber.log.Timber

class MediaItemViewHolder(
    private val binding: MediaItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): MediaItemViewHolder {
            val binding = MediaItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
            return MediaItemViewHolder(binding)
        }
    }

    fun bind(mediaItem: MediaItemData) {
        binding.title.text = mediaItem.title
        binding.subtitle.text = mediaItem.subtitle

        try {
            val art = if (mediaItem.albumArtUri != null && mediaItem.albumArtUri != Uri.EMPTY) {
                Glide.with(binding.root)
                    .load(mediaItem.albumArtUri)
            } else {
                Glide.with(binding.root)
                    .load(mediaItem.albumArt)
            }
            art.transform(RoundedCorners(20))
                .placeholder(R.drawable.ic_music)
                .error(R.drawable.ic_music)
                .into(binding.albumArt)
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }

}
