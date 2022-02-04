package com.sadique.messo.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.sadique.messo.R
import com.sadique.messo.databinding.ActivityMainBinding
import com.sadique.messo.ui.fragments.NowPlayingFragment
import com.sadique.messo.ui.viewmodels.MainActivityViewModel
import com.sadique.musicservice.utils.Status
import dagger.hilt.android.AndroidEntryPoint

private const val MY_SHARED_PREFERENCE = "my_shared_preference"

@AndroidEntryPoint
class MainActivity : PermissionActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    val motionLayout: MotionLayout get() = binding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        sharedPreferences = applicationContext.getSharedPreferences(
            MY_SHARED_PREFERENCE,
            MODE_PRIVATE
        )
        layout = binding.root
        getExternalStoragePermission()
    }

    override fun onPermissionGranted() {
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.bottomNavigation.setupWithNavController(navHostFragment.navController)
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> layout.showSnackbar(
                        binding.root,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG,
                        null
                    ) {}
                    else -> Unit
                }
            }
        }
        viewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> layout.showSnackbar(
                        binding.root,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG,
                        null
                    ) {}
                    else -> Unit
                }
            }
        }
    }

    fun startNowPlayingFragment() {
        if (supportFragmentManager.findFragmentByTag(NowPlayingFragment.TAG) == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<NowPlayingFragment>(R.id.now_playing_fragment_container, NowPlayingFragment.TAG)
            }
        }
    }
}