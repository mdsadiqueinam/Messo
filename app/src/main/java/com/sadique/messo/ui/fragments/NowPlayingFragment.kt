package com.sadique.messo.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.button.MaterialButton
import com.sadique.messo.R
import com.sadique.messo.databinding.NowPlayingFragmentBinding
import com.sadique.messo.extensions.lightenColor
import com.sadique.messo.models.MediaItemData
import com.sadique.messo.ui.activity.MainActivity
import com.sadique.messo.ui.viewmodels.MainActivityViewModel
import com.sadique.messo.ui.viewmodels.NowPlayingViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlin.math.abs


@AndroidEntryPoint
class NowPlayingFragment : Fragment() {

    companion object {
        const val TAG = "NowPlayingFragment"
        fun newInstance() = NowPlayingFragment()
    }

    private lateinit var binding: NowPlayingFragmentBinding

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: NowPlayingViewModel by viewModels()

    private var currentlyPlayingSong: MediaItemData? = null
    private var currentAlbumArt: Bitmap? = null
    private var shouldUpdateSeekbar: Boolean = true

    private val requestListener = object : RequestListener<Bitmap?> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Bitmap?>?,
            isFirstResource: Boolean,
        ): Boolean {
            changeBackgroundToDefault()
            return false
        }

        override fun onResourceReady(
            resource: Bitmap?,
            model: Any?,
            target: Target<Bitmap?>?,
            dataSource: DataSource?,
            isFirstResource: Boolean,
        ): Boolean {
            if (resource != currentAlbumArt) {
                currentAlbumArt = resource
                createPaletteAsync()
            }
            return false
        }

    }

    private val transitionListener = object : MotionLayout.TransitionListener {
        override fun onTransitionStarted(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Int,
        ) = Unit

        override fun onTransitionChange(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Int,
            progress: Float,
        ) {
            (activity as MainActivity).apply {
                this.motionLayout.setTransition(startId, endId)
                this.motionLayout.progress = progress
            }
        }

        override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) = Unit

        override fun onTransitionTrigger(
            motionLayout: MotionLayout?,
            triggerId: Int,
            positive: Boolean,
            progress: Float,
        ) = Unit

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = NowPlayingFragmentBinding.inflate(inflater,
            container,
            false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initView()
    }


    private fun NowPlayingFragmentBinding.initView() {

        playPauseBtn.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playOrToggleMedia(it, true)
            }
        }

        minimizedPlayPauseBtn.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playOrToggleMedia(it, true)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    songPosition.text = MediaItemData.timestampToMSS(context!!, progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })

        skipPrevBtn.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        skipNextBtn.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        minimizedSkipNextBtn.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        shuffleToggle.setOnClickListener {
            @PlaybackStateCompat.ShuffleMode
            when (viewModel.shuffleMode.value) {
                SHUFFLE_MODE_ALL -> mainViewModel.shuffleNone()
                else -> mainViewModel.shuffleAll()
            }
        }

        repeatToggle.setOnClickListener {
            @PlaybackStateCompat.RepeatMode
            when (viewModel.repeatMode.value) {
                REPEAT_MODE_ALL -> mainViewModel.repeatOne()
                REPEAT_MODE_ONE -> mainViewModel.repeatNone()
                else -> mainViewModel.repeatAll()
            }
        }

        root.addTransitionListener(transitionListener)

        subscribeToObservers()
    }

    private fun NowPlayingFragmentBinding.subscribeToObservers() {
        // Attach observers to the LiveData coming from this ViewModel
        viewModel.mediaMetadata.observe(viewLifecycleOwner) {
            currentlyPlayingSong = it
            updateUI()
        }
        viewModel.mediaButtonRes.observe(viewLifecycleOwner) {
            playPauseBtn.setIconResource(it)
            minimizedPlayPauseBtn.setIconResource(it)
        }
        viewModel.mediaPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekbar) {
                seekBar.progress = it.toInt()
                songPosition.text = MediaItemData.timestampToMSS(requireContext(), it)
            }
        }
        viewModel.shuffleMode.observe(viewLifecycleOwner) {
            @PlaybackStateCompat.ShuffleMode
            when (it) {
                SHUFFLE_MODE_ALL -> shuffleToggle.setIconTintResource(R.color.secondary)
                else -> shuffleToggle.iconTint = ColorStateList.valueOf(Color.WHITE)
            }
        }
        viewModel.repeatMode.observe(viewLifecycleOwner) {
            @PlaybackStateCompat.RepeatMode
            when (it) {
                REPEAT_MODE_ALL -> repeatToggle.setIconTintResource(R.color.secondary)
                REPEAT_MODE_ONE -> repeatToggle.setIconResource(R.drawable.ic_repeat_one_active)
                else -> {
                    repeatToggle.setIconResource(R.drawable.ic_repeat)
                    repeatToggle.iconTint = ColorStateList.valueOf(Color.WHITE)
                }
            }
        }
    }


    private fun NowPlayingFragmentBinding.updateUI() =
        currentlyPlayingSong?.let {
            manageAlbumArt(it.albumArtUri, it.albumArt)
            it.duration?.let { duration ->
                seekBar.max = duration.toInt()
            }
            songTitle.text = it.title
            minimizedSongTitle.text = it.title
            songArtist.text = it.subtitle
            minimizedSongArtist.text = it.subtitle
            songDuration.text = MediaItemData.timestampToMSS(requireContext(), it.duration)
        }

    private fun manageAlbumArt(albumArtUri: Uri?, albumArt: Bitmap?) {
        try {
            val art =
                if (albumArtUri != null && albumArtUri != Uri.EMPTY) {
                    Glide.with(binding.root)
                        .asBitmap()
                        .load(albumArtUri)
                } else {
                    Glide.with(binding.root)
                        .asBitmap()
                        .load(albumArt)
                }
            art.transition(BitmapTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.music)
                .transform(RoundedCorners(15))
                .addListener(requestListener)
                .into(binding.albumArt)
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }

    private fun createPaletteAsync() = currentAlbumArt?.let {
        Palette.from(it).generate { palette ->
            val darkVibrantSwatch = palette?.darkVibrantSwatch
            val current = binding.nowPlayingBackgroundView.background.current
            val colorDrawables = arrayOf(current,
                ColorDrawable(darkVibrantSwatch?.rgb
                    ?: ContextCompat.getColor(requireContext(), R.color.primaryDark))
            )
            binding.nowPlayingBackgroundView.setBackgroundColor(colorDrawables)
            binding.playPauseBtn.setBackgroundColor(lightenColor(darkVibrantSwatch?.rgb, 0.2F)
                ?: ContextCompat.getColor(requireContext(), R.color.primaryDark)
            )
        }
    }

    private fun changeBackgroundToDefault() {
        val current = binding.nowPlayingBackgroundView.background.current
        val colorDrawables = arrayOf(current,
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.primaryDark))
        )
        binding.nowPlayingBackgroundView.setBackgroundColor(colorDrawables)
        binding.playPauseBtn.setBackgroundColor(ContextCompat.getColor(requireContext(),
            R.color.primaryDark))
    }

    private fun CardView.setBackgroundColor(colorDrawables: Array<Drawable>) {
        val transitionDrawable = TransitionDrawable(colorDrawables)
        this.background = transitionDrawable
        transitionDrawable.startTransition(1000)
    }

    private fun MaterialButton.setBackgroundColor(colorDrawables: Array<Drawable>) {
        val transitionDrawable = TransitionDrawable(colorDrawables)
        this.background = transitionDrawable
        transitionDrawable.startTransition(200)
    }
}
