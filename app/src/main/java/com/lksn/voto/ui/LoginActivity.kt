package com.lksn.voto.ui

import android.content.Intent
import com.lksn.voto.databinding.ActivityLoginBinding
import android.os.Bundle
import android.os.Message
import android.provider.ContactsContract
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.lifecycleScope
import com.lksn.voto.R
import com.lksn.voto.data.ApiClient
import com.lksn.voto.data.ApiResult
import com.lksn.voto.data.AuthRequest
import com.lksn.voto.data.CartManager
import com.lksn.voto.data.Session
import com.lksn.voto.data.apiCall
import com.lksn.voto.util.hideKeyboard
import com.lksn.voto.util.toast
import com.lksn.voto.util.toastLong
import kotlinx.coroutines.launch
import kotlin.jvm.java

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.clear()
        CartManager.unload()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else false
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        binding.etEmail.error = null
        binding.etPassword.error = null

        var valid = true
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.err_email_empty)
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.err_email_invalid)
            valid = false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.err_password_empty)
            valid = false
        }
        if (!valid) return

        binding.root.hideKeyboard()
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            val login = apiCall(this@LoginActivity) {
                ApiClient.api.login(AuthRequest(email, password))
            }
            when (login) {
                is ApiResult.Success -> {
                    val token = login.data.string().trim().trim('"')
                    Session.token = token
                    loadProfileAndContinue(email)
                }
                is ApiResult.Failure -> {
                    binding.btnLogin.isEnabled = true
                    if (login.code == 404) toast(getString(R.string.err_login_failed))
                    else toastLong(login.message)
                }
            }
        }
    }

    private suspend fun loadProfileAndContinue(typedEmail: String) {
        when (val me = apiCall(this) { ApiClient.api.getMe() }) {
            is ApiResult.Success -> {
                Session.user = me.data
                CartManager.load(this, me.data.email ?: typedEmail)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            is ApiResult.Failure -> {
                Session.clear()
                binding.btnLogin.isEnabled = true
                toastLong(me.message)
            }
        }
    }
}