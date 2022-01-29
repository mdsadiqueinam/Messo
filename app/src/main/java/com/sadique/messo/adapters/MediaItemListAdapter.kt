package com.sadique.messo.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.sadique.messo.models.MediaItemData
import com.sadique.messo.ui.viewholders.MediaItemViewHolder

class MediaItemListAdapter(
    private val itemClickedListener: (MediaItemData) -> Unit,
) : ListAdapter<MediaItemData, MediaItemViewHolder>(MediaItemData.diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        return MediaItemViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder, position: Int) {
        val data: MediaItemData = getItem(position)
        holder.bind(data)
        holder.itemView.setOnClickListener {
            itemClickedListener(data)
        }
    }
}