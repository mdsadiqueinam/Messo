package com.sadique.messo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sadique.messo.databinding.FragmentPlayScreenBinding

class PlayScreenFragment : Fragment() {

    companion object {
        const val TAG = "PlayScreenFragment"
        fun newInstance(): PlayScreenFragment {
            return PlayScreenFragment()
        }
    }

    lateinit var fragmentPlayScreenBinding: FragmentPlayScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        fragmentPlayScreenBinding = FragmentPlayScreenBinding.inflate(inflater, container, false)
        return fragmentPlayScreenBinding.root
    }
}