package com.shakuro.firebaseproject.activities

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import com.shakuro.firebaseproject.BuildConfig
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.utils.ImageUtils
import com.shakuro.firebaseproject.utils.PermissionUtil
import com.shakuro.firebaseproject.utils.RealPathUtil
import com.shakuro.firebaseproject.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_user.*
import java.io.File
import java.io.IOException
import java.util.*

class UserActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val REQUEST_CAMERA = 0
        private const val REQUEST_GALLERY = 1

        private const val TAKE_PICTURE_FROM_CAMERA = 707
        private const val TAKE_PICTURE_FROM_GALLERY = 708
    }

    private lateinit var choiceData: MutableList<String>

    private lateinit var imageUri: Uri
    private lateinit var cameraFileCaptured: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        editProfileAvatar.setOnClickListener(this)
        applyButton.setOnClickListener(this)

        choiceData = ArrayList()
        choiceData.add(getString(R.string.edit_profile_choice_avatar_from_camera))
        choiceData.add(getString(R.string.edit_profile_choice_avatar_from_gallery))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.editProfileAvatar -> {
                initChoiceDialog()
            }

            R.id.applyButton -> {

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initChoiceDialog() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, choiceData)
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.edit_profile_choice_avatar_title))
        dialog.setSingleChoiceItems(adapter, -1, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> {
                    getImageFromCamera()
                    dialog.dismiss()
                }
                1 -> {
                    getImageFromGallery()
                    dialog.dismiss()
                }
            }
        })
        dialog.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        dialog.create().show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun getImageFromCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        } else {
            showCameraPreview()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun getImageFromGallery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestGalleryPermission()
        } else {
            showGallery()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA)) {
            showAlertDialog(null, getString(R.string.permission_camera_rationale), DialogInterface.OnClickListener { dialog, which ->
                requestPermissions(
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CAMERA)
            })
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestGalleryPermission() {
        if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showAlertDialog(null, getString(R.string.permission_read_external_storage_rationale), DialogInterface.OnClickListener { dialog, which ->
                requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_GALLERY)
            })
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == TAKE_PICTURE_FROM_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageUri

                try {
                    contentResolver.notifyChange(selectedImage, null)
                    var bitmap = ImageUtils.decodeSampledBitmapFromResource(this, selectedImage)
                    if (cameraFileCaptured != null && cameraFileCaptured.path != null) {
                        val realPath = cameraFileCaptured.path
                        bitmap = ImageUtils.getCorrectBitmapOrientation(bitmap, realPath)
                        editProfileAvatar.setImageBitmap(getResizedAvatarBitmap(bitmap))
                    }


                } catch (e: Exception) {
                    Log.e("Camera", e.toString())
                    showError(getString(R.string.camera_error_message))
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                revokeUriPermission(imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else if (requestCode == TAKE_PICTURE_FROM_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the url from data
                val selectedImageUri = data.data
                if (null != selectedImageUri) {
                    try {
                        var bitmap = ImageUtils.decodeSampledBitmapFromResource(this, selectedImageUri)

                        var realPath: String? = null
                        // SDK < API11
                        if (Build.VERSION.SDK_INT < 11)
                            realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, selectedImageUri)
                        else if (Build.VERSION.SDK_INT < 19)
                            realPath = RealPathUtil.getRealPathFromURI_API11to18(this, selectedImageUri)
                        else
                            realPath = RealPathUtil.getRealPathFromURI_API19(this, selectedImageUri)// SDK > 19 (Android 4.4)
                        // SDK >= 11 && SDK < 19

                        if (realPath != null) {
                            bitmap = ImageUtils.getCorrectBitmapOrientation(bitmap, realPath)
                            editProfileAvatar.setImageBitmap(getResizedAvatarBitmap(bitmap))
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT//
        startActivityForResult(Intent.createChooser(intent, "Select File"), TAKE_PICTURE_FROM_GALLERY)
    }

    private fun showCameraPreview() {
        // create Intent to take a picture and return control to the calling application
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            cameraFileCaptured = File(filesDir, "captured.jpg")
            if (!cameraFileCaptured.exists()) {
                cameraFileCaptured.createNewFile()
            }

            if (cameraFileCaptured != null) {
                imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", cameraFileCaptured)

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    val resolvedIntentActivities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    for (resolvedIntentInfo in resolvedIntentActivities) {
                        val packageName = resolvedIntentInfo.activityInfo.packageName

                        grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }

                startActivityForResult(intent, TAKE_PICTURE_FROM_CAMERA)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getResizedAvatarBitmap(bitmap: Bitmap): Bitmap {
        val previewInDp = 100
        val sizeInPx = ScreenUtils.convertDpToPixels(previewInDp.toFloat())

        return Bitmap.createScaledBitmap(bitmap, sizeInPx, sizeInPx, true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA) {
            var permisionAvailableCamera = true
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permisionAvailableCamera = false
                    break
                }
            }
            if (permisionAvailableCamera) {
                showMessage(getString(R.string.permision_available_camera))
                val handler = Handler()
                handler.post { showCameraPreview() }
            } else {
                showMessage(getString(R.string.permissions_not_granted))
            }
        } else if (requestCode == REQUEST_GALLERY) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                showMessage(getString(R.string.permision_available_read_external_storage))
                val handler = Handler()
                handler.post { showGallery() }
            } else {
                showMessage(getString(R.string.permissions_not_granted))
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


}