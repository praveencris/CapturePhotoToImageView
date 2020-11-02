package com.sabkayar.praveen.capturedphototoimageview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.sabkayar.praveen.capturedphototoimageview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    val RC_CAMERA_PERMISSION = 2
    private lateinit var mBinding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
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
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Toast.makeText(this, R.string.camera_not_found_alert, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            mBinding.imvPhoto.setImageBitmap(imageBitmap)
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
}