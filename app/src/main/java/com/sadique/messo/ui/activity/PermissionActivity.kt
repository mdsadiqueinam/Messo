package com.sadique.messo.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sadique.messo.R
import timber.log.Timber

abstract class PermissionActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE_CODE = 101
        const val USER_ASKED_STORAGE_PERMISSION_BEFORE = "user_asked_storage_permission_before"
        const val TAG = "PermissionActivity"
    }

    lateinit var layout: View
    lateinit var sharedPreferences: SharedPreferences
    var isStoragePermissionGranted: Boolean = false

    abstract fun onPermissionGranted()

    fun getExternalStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                isStoragePermissionGranted = true
                onPermissionGranted()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                //Can ask user for permission
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE_CODE
                )
            }

            else -> {
                val userAskedPermissionBefore = sharedPreferences.getBoolean(
                    USER_ASKED_STORAGE_PERMISSION_BEFORE,
                    false
                )
                if (userAskedPermissionBefore) {
                    //If User was asked permission before and denied by don't ask
                    openSettings()
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
        }
    }

    private fun openSettings() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(R.string.permission_require)
        alertDialogBuilder.setMessage(R.string.storage_access_required_reason)
        alertDialogBuilder.setPositiveButton(
            R.string.open_settings
        ) { _, _ ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts(
                "package", this.packageName,
                null
            )
            intent.data = uri
            this.startActivity(intent)
        }
        alertDialogBuilder.setNegativeButton(
            R.string.cancel
        ) { _, _ ->
            Timber.d("onClick: Cancelling")
        }
        val dialog: AlertDialog = alertDialogBuilder.create()
        dialog.show()
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