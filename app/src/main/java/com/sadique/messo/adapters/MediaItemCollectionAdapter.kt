package com.sadique.messo.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sadique.messo.ui.fragments.MediaItemListFragment

class MediaItemCollectionAdapter(fragment: Fragment, private val rootIds: List<String>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = rootIds.size

    override fun createFragment(position: Int): Fragment {
        return MediaItemListFragment.newInstance(rootIds[position])
    }

}