package com.godlonton.isfood

import android.support.design.widget.Snackbar
import android.view.View

fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
  val snack = Snackbar.make(this, message, length)
  snack.f()
  snack.show()
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
  setAction(action, listener)
  color?.let { setActionTextColor(color) }
}