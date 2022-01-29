package com.sadique.messo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sadique.messo.databinding.NowPlayingFragmentBinding


class NowPlayFragment : Fragment() {

    companion object {
        const val TAG = "NowPlayFragment"
    }

    private lateinit var binding: NowPlayingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = NowPlayingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

}