package com.sabkayar.praveen.capturedphototoimageview

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.sabkayar.praveen.capturedphototoimageview.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    val RC_CAMERA_PERMISSION = 2
    private lateinit var mBinding: ActivityMainBinding;
    var isOnCreateCalled: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        isOnCreateCalled = true
    }

    fun onTakePhotoClicked(view: View) {
        //1. Check for Platform
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //2. Check for permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PERMISSION_GRANTED
            ) {
                dispatchTakePictureIntent()
            } else {
                //3. Explain why permission needed
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    Toast.makeText(
                        this,
                        "Camera permission is needed for capturing image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //4. Request permission
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    RC_CAMERA_PERMISSION
                )
            }
        } else {
            dispatchTakePictureIntent()
        }
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.sabkayar.praveen.capturedphototoimageview",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*val imageBitmap = data?.extras?.get("data") as Bitmap
            mBinding.imvPhoto.setImageBitmap(imageBitmap)*/
            setPic(currentPhotoPath)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_CAMERA_PERMISSION) {
            //5. Handel the response
            //Check if permission granted
            if (grantResults[0] == PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                //Camera Permission was denied so we can't use this feature
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            SharedPref(this@MainActivity).saveVariable(R.string.pathLatestPhoto, currentPhotoPath)
        }
    }


    private fun setPic(currentPhotoPath: String) {
        // Get the dimensions of the View
        val targetW: Int = mBinding.imvPhoto.width
        val targetH: Int = mBinding.imvPhoto.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            var scaleFactor: Int
            try {
                scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH))
            } catch (e: Exception) {
                scaleFactor = 1
            }

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            mBinding.imvPhoto.setImageBitmap(bitmap)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isOnCreateCalled) {
            val photoPath = SharedPref(this).getVariable(R.string.pathLatestPhoto, "")
            if (photoPath!!.length > 0) {
                setPic(photoPath)
                isOnCreateCalled = false
            }
        }
    }
}