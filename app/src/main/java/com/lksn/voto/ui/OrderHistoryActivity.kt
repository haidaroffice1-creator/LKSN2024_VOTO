package com.lksn.voto.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.lksn.voto.R
import com.lksn.voto.data.ApiClient
import com.lksn.voto.data.ApiResult
import com.lksn.voto.data.Order
import com.lksn.voto.data.apiCall
import com.lksn.voto.databinding.ActivityOrderHistoryBinding
import com.lksn.voto.databinding.ItemOrderBinding
import com.lksn.voto.databinding.ItemOrderLineBinding
import com.lksn.voto.util.Format
import kotlinx.coroutines.launch

class OrderHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityOrderHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { goHome() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })

        lifecycleScope.launch {
            when (val result = apiCall(this@OrderHistoryActivity) {
                ApiClient.api.getMyTransaction()
            }) {
                is ApiResult.Success -> render(result.data)
                is ApiResult.Failure -> showFailure(result)
            }
        }
    }

    private fun render(orders: List<Order>) {
        val container = binding.orderContainer
        container.removeAllViews()
        binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE

        orders.forEach { order ->
            val card = ItemOrderBinding.inflate(layoutInflater, container, false)
            card.tvOrderId.text = order.id
            card.tvStatus.text = getString(R.string.status_format, order.status.orEmpty())
            card.tvOrderTotal.text = getString(R.string.total_format, Format.rupiahComma(order.totalPrice))

            order.transaction.orEmpty().forEach { line ->
                val row = ItemOrderLineBinding.inflate(layoutInflater, card.linesContainer, false)
                row.tvLineName.text = line.name
                row.tvLineQty.text = getString(R.string.qty_format, line.qty)
                row.tvLineSubtotal.text = Format.rupiahComma(line.subtotal)
                card.linesContainer.addView(row.root)
            }
            container.addView(card.root)
        }
    }
}