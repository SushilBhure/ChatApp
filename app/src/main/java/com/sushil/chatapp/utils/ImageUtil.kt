package com.sushil.chatapp.utils

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sushil.chatapp.R
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtil {

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryPermissionLauncher: ActivityResultLauncher<Array<String>>

    private var imageUri: Uri? = null
    private var onBase64Ready: ((String?) -> Unit)? = null

    fun register(fragment: Fragment, onBase64: (String?) -> Unit) {
        onBase64Ready = onBase64

        // Camera permission launcher
        cameraPermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                launchCameraInternal(fragment)
            } else {
                Toast.makeText(fragment.requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                onBase64Ready?.invoke(null)
            }
        }

        // Gallery permission launcher
        galleryPermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.values.all { it }
            if (granted) {
                launchGalleryInternal()
            } else {
                Toast.makeText(fragment.requireContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
                onBase64Ready?.invoke(null)
            }
        }

        // Image picker launcher
        pickImageLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { compressToBase64(fragment.requireContext(), it) } ?: onBase64Ready?.invoke(null)
        }

        // Take picture launcher
        takePhotoLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                imageUri?.let { compressToBase64(fragment.requireContext(), it) } ?: onBase64Ready?.invoke(null)
            } else {
                Toast.makeText(fragment.requireContext(), "Camera capture cancelled or failed", Toast.LENGTH_SHORT).show()
                onBase64Ready?.invoke(null)
            }
        }
    }

    fun pickImage(fragment: Fragment) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(fragment.requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            launchGalleryInternal()
        } else {
            galleryPermissionLauncher.launch(permissions)
        }
    }

    fun takePhoto(fragment: Fragment) {
        val context = fragment.requireContext()
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            launchCameraInternal(fragment)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchGalleryInternal() {
        pickImageLauncher.launch("image/*")
    }

    private fun launchCameraInternal(fragment: Fragment) {
        val context = fragment.requireContext()
        val photoFile = createImageFile(context)
        if (photoFile == null) {
            Toast.makeText(context, "Failed to create file for photo", Toast.LENGTH_SHORT).show()
            onBase64Ready?.invoke(null)
            return
        }
        imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
        imageUri?.let { takePhotoLauncher.launch(it) } ?: run {
            Toast.makeText(context, "Failed to get URI for photo file", Toast.LENGTH_SHORT).show()
            onBase64Ready?.invoke(null)
        }
    }

    private fun createImageFile(context: Context): File? {
        var storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir == null || !storageDir.exists()) {
            storageDir = context.cacheDir
        }
        if (storageDir != null && !storageDir.exists()) {
            val success = storageDir.mkdirs()
            if (!success) {
                Log.e("ImageUtils", "Failed to create directory: $storageDir")
                return null
            }
        }
        return if (storageDir != null) {
            File(storageDir, "photo_${System.currentTimeMillis()}.jpg")
        } else {
            Log.e("ImageUtils", "Storage directory is null")
            null
        }
    }

    private fun compressToBase64(context: Context, uri: Uri) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmapRaw = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val bitmap = rotateImageIfRequired(context, uri, bitmapRaw)

            val targetSizeBytes = 700 * 1024
            var quality = 100
            var byteArray: ByteArray

            do {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                byteArray = outputStream.toByteArray()
                quality -= 5
            } while (byteArray.size > targetSizeBytes && quality > 10)

            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            onBase64Ready?.invoke(base64String)

        } catch (e: Exception) {
            e.printStackTrace()
            onBase64Ready?.invoke(null)
        }
    }

    private fun rotateImageIfRequired(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = inputStream?.use { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap
        }
    }

    fun showImagePickerDialog(fragment: Fragment) {
        val context = fragment.context ?: return

        val options = arrayOf("Camera", "Gallery")

        MaterialAlertDialogBuilder(context)
            .setTitle("Pick Image")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhoto(fragment)
                    1 -> pickImage(fragment)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun loadBase64IntoImageView(base64String: String, imageView: ImageView) {
        try {
            val pureBase64 = base64String.substringAfter(",") // Remove "data:image..." if present
            val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)

            Glide.with(imageView.context)
                .asBitmap()
                .load(imageBytes) // Use ByteArray, not InputStream
                .placeholder(R.drawable.round_profile)
                .error(R.drawable.round_profile)
                .transform(CircleCrop())
                .into(imageView)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}