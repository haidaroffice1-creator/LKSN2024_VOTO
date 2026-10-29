package com.lksn.voto.ui

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.lksn.voto.R
import androidx.lifecycle.lifecycleScope
import com.lksn.voto.data.ApiClient
import com.lksn.voto.data.ApiResult
import com.lksn.voto.data.CartManager
import com.lksn.voto.data.Session
import com.lksn.voto.data.TransactionItemRequest
import com.lksn.voto.data.TransactionRequest
import com.lksn.voto.data.apiCall
import com.lksn.voto.databinding.ActivityCheckoutBinding
import com.lksn.voto.util.Format
import com.lksn.voto.util.hideKeyboard
import com.lksn.voto.util.toast
import com.lksn.voto.util.toastLong
import kotlinx.coroutines.launch
import kotlin.jvm.java

class CheckoutActivity : BaseActivity() {
    companion object {
        const val DELIVERY_FEE = 30_000.0
        private const val VOTOKEN_RATE = 0.001
    }
    private lateinit var binding: ActivityCheckoutBinding
    private var subtotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = CartManager.getCheckedItems()
        if (items.isEmpty()) {
            finish()
            return
        }

        subtotal = CartManager.checkedTotal()
        binding.tvSubtotal.text = Format.rupiah(subtotal)
        binding.tvDelivery.text = Format.rupiah(DELIVERY_FEE)
        binding.tvTotal.text = getString(R.string.total_format, Format.rupiahComma(subtotal + DELIVERY_FEE))
        binding.btnBack.setOnClickListener { finish() }
        binding.cbUseMyInfo.setOnClickListener { applyUseMyInfo(binding.cbUseMyInfo.isChecked) }
        binding.tvUseMyInfo.setOnClickListener { binding.cbUseMyInfo.toggle()
        applyUseMyInfo(binding.cbUseMyInfo.isChecked) }
        binding.etAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.root.hideKeyboard()
                true
            } else false
        }
        binding.btnCheckout.setOnClickListener { submit() }
    }

    private fun applyUseMyInfo(use: Boolean) {
        val user = Session.user
        if (use && user != null) {
            binding.etName.setText(user.name.orEmpty())
            binding.etPhone.setText(user.phoneNumber.orEmpty())
            binding.etAddress.setText(user.address.orEmpty())
        } else if (!use) {
            binding.etName.text = null
            binding.etPhone.text = null
            binding.etAddress.text = null
        }
        listOf(binding.etName, binding.etPhone, binding.etAddress).forEach {
            it.isEnabled = !use
            it.error = null
        }
    }
    private fun submit() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        binding.etName.error = null
        binding.etPhone.error = null
        binding.etAddress.error = null

        var valid = true
        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.err_name_empty)
            valid = false
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = getString(R.string.err_phone_empty)
            valid = false
        } else if (!Regex("^\\+?[0-9]{8,15}$").matches(phone)) {
            binding.etPhone.error = getString(R.string.err_phone_invalid)
            valid = false
        }
        if (address.isEmpty()) {
            binding.etAddress.error = getString(R.string.err_address_empty)
            valid = false
        }
        if (!valid) return

        val items = CartManager.getCheckedItems()
        if (items.isEmpty()) {
            toast(getString(R.string.err_no_item_selected))
            return
        }

        // TotalPrice = price of the products (the 0.1% Votoken is calculated from it by the server).
        // The fixed delivery fee is only added to what the customer pays and is not part of this value.
        val request = TransactionRequest(
            recipientName = name,
            recipientPhoneNumber = phone,
            shippingAddress = address,
            totalPrice = subtotal,
            items = items.map { TransactionItemRequest(it.cameraId, it.qty, it.price * it.qty) }
        )

        binding.root.hideKeyboard()
        binding.btnCheckout.isEnabled = false
        lifecycleScope.launch {
            when (val result = apiCall(this@CheckoutActivity) { ApiClient.api.addTransaction(request) }) {
                is ApiResult.Success -> {
                    CartManager.removeChecked()
                    val earned = Math.round(subtotal * VOTOKEN_RATE).toInt()
                    toastLong(getString(R.string.checkout_success_format, earned))
                    startActivity(Intent(this@CheckoutActivity, OrderHistoryActivity::class.java))
                    finish()
                }
                is ApiResult.Failure -> {
                    binding.btnCheckout.isEnabled = true
                    showFailure(result)
                }
            }
        }
    }
}