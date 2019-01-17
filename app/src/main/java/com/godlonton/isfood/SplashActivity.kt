package com.godlonton.isfood

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.godlonton.isfood.R

class SplashActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    makeFullScreen()

    setContentView(R.layout.activity_splash)

    // Using a handler to delay loading the MainActivity
    Handler().postDelayed({

      // Start activity
      startActivity(Intent(this, MainActivity::class.java))

      // Animate the loading of new activity
      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

      // Close this activity
      finish()

    }, 2000)
  }

  private fun makeFullScreen() {
    // Remove Title
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    // Make Fullscreen
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN)

    // Hide the toolbar
    supportActionBar?.hide()
  }
}
