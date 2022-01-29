package com.sadique.messo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.sadique.messo.adapters.MediaItemListAdapter
import com.sadique.messo.databinding.MediaItemListFragmentBinding
import com.sadique.messo.models.MediaItemData
import com.sadique.messo.ui.activity.MainActivity
import com.sadique.messo.ui.viewmodels.MainActivityViewModel
import com.sadique.messo.ui.viewmodels.MediaItemListViewModel
import com.sadique.messo.utils.Constants.ARG_ROOT_ID
import com.sadique.musicservice.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaItemListFragment : Fragment() {

    companion object {
        fun newInstance(mediaId: String) = MediaItemListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ROOT_ID, mediaId)
            }
        }
    }

    private lateinit var binding: MediaItemListFragmentBinding

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: MediaItemListViewModel by viewModels()

    private val listAdapter: MediaItemListAdapter by lazy {
        MediaItemListAdapter {
            mainViewModel.playOrToggleMedia(it, false)
            (activity as MainActivity).startNowPlayingFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = MediaItemListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.subscribeToObservers()
    }

    private fun MediaItemListFragmentBinding.subscribeToObservers() {
        viewModel.mediaItems.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    loadingSpinner.visibility = View.VISIBLE
                }
                Status.SUCCESS -> manageList(it.data, "No Media Items to show")
                Status.ERROR -> manageList(it.data, it.message)
            }
        }

        viewModel.networkError.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        loadingSpinner.visibility = View.GONE
                        networkError.visibility = View.VISIBLE
                    }
                    else -> {
                        networkError.visibility = View.GONE
                    }
                }
            }
        }

        list.adapter = listAdapter
    }

    private fun MediaItemListFragmentBinding.manageList(
        data: List<MediaItemData>?,
        message: String?,
    ) {
        loadingSpinner.visibility = View.GONE
        if (data?.isNotEmpty() == true) {
            listDetail.visibility = View.GONE
            listAdapter.submitList(data)
        } else {
            listDetail.visibility = View.VISIBLE
            listDetail.text = message
        }
    }
}