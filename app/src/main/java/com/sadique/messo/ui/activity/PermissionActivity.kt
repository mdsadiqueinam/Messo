package com.sadique.messo.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sadique.messo.R
import timber.log.Timber

abstract class PermissionActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE_CODE = 101
        const val USER_ASKED_STORAGE_PERMISSION_BEFORE = "user_asked_storage_permission_before"
    }

    lateinit var layout: View
    lateinit var sharedPreferences: SharedPreferences
    var isStoragePermissionGranted: Boolean = false

    abstract fun onPermissionGranted()

    fun getExternalStoragePermission() {
        when {
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )) -> {
                //Can ask user for permission
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE_CODE
                )
            }

            (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) -> {
                val userAskedPermissionBefore = sharedPreferences.getBoolean(
                    USER_ASKED_STORAGE_PERMISSION_BEFORE,
                    false
                )
                if (userAskedPermissionBefore) {
                    //If User was asked permission before and denied by don't ask
                    openSettings(
                        message = R.string.storage_access_required_reason,
                        positiveButtonClickListener = { _, _ ->
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri: Uri = Uri.fromParts(
                                "package", this.packageName,
                                null
                            )
                            intent.data = uri
                            this.startActivity(intent)
                        },
                        negativeButtonClickListener = { _, _ ->
                            Timber.d("onClick: Cancelling")
                        }
                    )
                } else {
                    //If user is asked permission for first time
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_READ_EXTERNAL_STORAGE_CODE
                    )
                    val editor = sharedPreferences.edit()
                    editor.putBoolean(USER_ASKED_STORAGE_PERMISSION_BEFORE, true)
                    editor.apply()
                }
            }

            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) -> {
                openSettings(
                    message = R.string.access_all_files,
                    positiveButtonClickListener = { _, _ ->
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        this.startActivity(intent)
                    },
                    negativeButtonClickListener = { _, _ ->
                        Timber.d("onClick: Cancelling")
                    }
                )
            }

            else -> {
                isStoragePermissionGranted = true
                onPermissionGranted()
            }
        }
    }

    private fun openSettings(
        message: Int,
        positiveButtonClickListener: DialogInterface.OnClickListener,
        negativeButtonClickListener: DialogInterface.OnClickListener? = null,
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_require)
            .setMessage(message)
            .setPositiveButton(
                R.string.open_settings,
                positiveButtonClickListener
            )
            .setNegativeButton(
                R.string.cancel,
                negativeButtonClickListener
            )
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isStoragePermissionGranted = false
        when (requestCode) {
            PERMISSION_REQUEST_READ_EXTERNAL_STORAGE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Granted
                    isStoragePermissionGranted = true
                    onPermissionGranted()
                } else {
                    //Denied
                    layout.showSnackbar(
                        layout,
                        getString(R.string.storage_access_required_reason),
                        Snackbar.LENGTH_INDEFINITE,
                        getString(R.string.ok)
                    ) {
                        getExternalStoragePermission()
                    }
                }
            }
        }
    }

    fun View.showSnackbar(
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit,
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        } else {
            snackbar.show()
        }
    }

}