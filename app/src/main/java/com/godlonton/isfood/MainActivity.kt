package com.godlonton.isfood

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions
import com.mindorks.paracamera.Camera
import com.godlonton.isfood.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private lateinit var camera: Camera
  private val PERMISSION_REQUEST_CODE = 1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Init Firebase
    FirebaseApp.initializeApp(this)

    // Configure Camera
    camera = Camera.Builder()
        .resetToCorrectOrientation(true)//1
        .setTakePhotoRequestCode(Camera.REQUEST_TAKE_PHOTO)//2
        .setDirectory("pics")//3
        .setName("isfood_${System.currentTimeMillis()}")//3
        .setImageFormat(Camera.IMAGE_JPEG)//4
        .setCompression(75)//5
        .build(this)
  }

  fun takePicture(view: View) {
    if (!hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
        !hasPermission(android.Manifest.permission.CAMERA)) {
      // If do not have permissions then request it
      requestPermissions()
    } else {
      // else all permissions granted, go ahead and take a picture using camera
      try {
        camera.takePicture()
      } catch (e: Exception) {
        // Show a toast for exception
        Toast.makeText(this.applicationContext, getString(R.string.error_taking_picture),
            Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun requestPermissions() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

      mainLayout.snack(getString(R.string.permission_message), Snackbar.LENGTH_INDEFINITE) {
        action(getString(R.string.OK)) {
          ActivityCompat.requestPermissions(this@MainActivity,
              arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  android.Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
        }
      }
    } else {
      ActivityCompat.requestPermissions(this,
          arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
              android.Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
      return
    }
  }

  private fun hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this,
        permission) == PackageManager.PERMISSION_GRANTED

  }

  override fun onRequestPermissionsResult(requestCode: Int,
      permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
      PERMISSION_REQUEST_CODE -> {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
          try {
            camera.takePicture()
          } catch (e: Exception) {
            Toast.makeText(this.applicationContext, getString(R.string.error_taking_picture),
                Toast.LENGTH_SHORT).show()
          }
        }
        return
      }
    }
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == Camera.REQUEST_TAKE_PHOTO) {
        val bitmap = camera.cameraBitmap
        if (bitmap != null) {
          imageView.setImageBitmap(bitmap)
          detectDeliciousFoodOnDevice(bitmap)
        } else {
          Toast.makeText(this.applicationContext, getString(R.string.picture_not_taken),
              Toast.LENGTH_SHORT).show()
        }
      }
    }
  }


  private fun displayResultMessage(hasDeliciousFood: Boolean) {
    responseCardView.visibility = View.VISIBLE

    if (hasDeliciousFood) {
      responseCardView.setCardBackgroundColor(Color.GREEN)
      responseTextView.text = getString(R.string.delicious_food)
    } else {
      responseCardView.setCardBackgroundColor(Color.RED)
      responseTextView.text = getString(R.string.not_delicious_food)
    }
  }

  private fun hasDeliciousFood(items: List<String>): Boolean {
    for (result in items) {
      if (result.contains("Food", true))
        return true
    }
    return false
  }

  private fun detectDeliciousFoodOnDevice(bitmap: Bitmap) {
    //1
    progressBar.visibility = View.VISIBLE
    val image = FirebaseVisionImage.fromBitmap(bitmap)
    val options = FirebaseVisionLabelDetectorOptions.Builder()
        .setConfidenceThreshold(0.8f)
        .build()
    val detector = FirebaseVision.getInstance().getVisionLabelDetector(options)

    //2
    detector.detectInImage(image)
        //3
        .addOnSuccessListener {

          progressBar.visibility = View.INVISIBLE

          if (hasDeliciousFood(it.map { it.label.toString() })) {
            displayResultMessage(true)
          } else {
            displayResultMessage(false)
          }

        }//4
        .addOnFailureListener {
          progressBar.visibility = View.INVISIBLE
          Toast.makeText(this.applicationContext, getString(R.string.error),
              Toast.LENGTH_SHORT).show()

        }
  }

  private fun detectDeliciousFoodOnCloud(bitmap: Bitmap) {
    progressBar.visibility = View.VISIBLE
    val image = FirebaseVisionImage.fromBitmap(bitmap)
    val options = FirebaseVisionCloudDetectorOptions.Builder()
        .setMaxResults(10)
        .build()
    val detector = FirebaseVision.getInstance()
        //1
        .getVisionCloudLabelDetector(options)

    detector.detectInImage(image)
        .addOnSuccessListener {

          progressBar.visibility = View.INVISIBLE

          if (hasDeliciousFood(it.map { it.label.toString() })) {
            displayResultMessage(true)
          } else {
            displayResultMessage(false)
          }

        }
        .addOnFailureListener {
          progressBar.visibility = View.INVISIBLE
          Toast.makeText(this.applicationContext, getString(R.string.error),
              Toast.LENGTH_SHORT).show()

        }
  }
}






 
