package com.lksn.voto.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.lksn.voto.R
import com.lksn.voto.data.ApiClient

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.toastLong(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
fun View.hideKeyboard() {
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken,0)
}

fun ImageView.loadPhoto(photo: String?) {
    Glide.with(this).load(ApiClient.imageUrl(photo)).into(this)
}

fun Boolean.toYesNo(context: Context): String =
    context.getString(if (this) R.string.yes else R.string.no)