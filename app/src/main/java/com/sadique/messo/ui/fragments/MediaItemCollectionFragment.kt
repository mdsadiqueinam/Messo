package com.sadique.messo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sadique.messo.adapters.MediaItemCollectionAdapter
import com.sadique.messo.databinding.MediaItemCollectionFragmentBinding
import com.sadique.messo.ui.viewmodels.MediaItemCollectionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaItemCollectionFragment : Fragment() {

    companion object {
        fun newInstance() = MediaItemCollectionFragment()
    }

    private lateinit var _binding: MediaItemCollectionFragmentBinding
    private val binding: MediaItemCollectionFragmentBinding get() = _binding

    private lateinit var viewModel: MediaItemCollectionViewModel
    private lateinit var adapter: MediaItemCollectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = MediaItemCollectionFragmentBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MediaItemCollectionAdapter(this, listOf("__LOCAL__"))
        binding.viewPager.adapter = adapter

    }


}