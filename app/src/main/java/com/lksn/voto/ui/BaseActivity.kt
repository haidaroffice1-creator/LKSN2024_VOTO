package com.lksn.voto.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.lksn.voto.data.ApiResult
import com.lksn.voto.data.CartManager
import com.lksn.voto.data.Session
import com.lksn.voto.util.toastLong

abstract class BaseActivity : AppCompatActivity() {
    protected fun showFailure(failure: ApiResult.Failure) {
        toastLong(failure.message)
        if (failure.isUnauthorized) {
            Session.clear()
            CartManager.unload()
            startActivity(Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }

    protected fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
        finish()
    }
}