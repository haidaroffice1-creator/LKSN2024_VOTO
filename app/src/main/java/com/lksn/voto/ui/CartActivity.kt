package com.lksn.voto.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import com.lksn.voto.R
import com.lksn.voto.data.CartManager
import com.lksn.voto.databinding.ActivityCartBinding
import com.lksn.voto.databinding.ItemCartItemBinding
import com.lksn.voto.databinding.ItemCartShopBinding
import com.lksn.voto.util.Format
import com.lksn.voto.util.loadPhoto
import com.lksn.voto.util.toast
import android.content.Intent
import kotlin.jvm.java

class CartActivity : BaseActivity() {

    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { goHome() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })

        binding.btnCheckout.setOnClickListener {
            if (CartManager.getCheckedItems().isEmpty()) {
                toast(getString(R.string.err_no_item_selected))
            } else {
                startActivity(Intent(this, CheckoutActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    /** Rebuilds the grouped list (cart size is small, so a full rebuild is simple and always consistent). */
    private fun render() {
        val container = binding.cartContainer
        container.removeAllViews()

        val shops = CartManager.getShops()
        binding.tvEmpty.visibility = if (shops.isEmpty()) View.VISIBLE else View.GONE

        shops.forEachIndexed { index, shop ->
            val shopView = ItemCartShopBinding.inflate(layoutInflater, container, false)
            if (index > 0) {
                (shopView.root.layoutParams as LinearLayout.LayoutParams).topMargin = dp(56)
            }
            shopView.tvShop.text = shop
            shopView.cbShop.isChecked = CartManager.isShopChecked(shop)
            shopView.cbShop.setOnClickListener {
                CartManager.setShopChecked(shop, shopView.cbShop.isChecked)
                render()
            }

            CartManager.itemsOf(shop).forEach { item ->
                val row = ItemCartItemBinding.inflate(layoutInflater, shopView.itemsContainer, false)
                row.ivPhoto.loadPhoto(item.photo)
                row.tvName.text = item.name
                row.tvQty.text = item.qty.toString()
                row.tvPrice.text = Format.rupiahComma(item.price * item.qty)
                row.cbItem.isChecked = item.checked
                row.cbItem.setOnClickListener {
                    CartManager.setChecked(item.cameraId, row.cbItem.isChecked)
                    render()
                }
                row.btnPlus.setOnClickListener {
                    CartManager.increase(item.cameraId)
                    render()
                }
                row.btnMinus.setOnClickListener {
                    CartManager.decrease(item.cameraId) // removes the product when it reaches zero
                    render()
                }
                shopView.itemsContainer.addView(row.root)
            }
            container.addView(shopView.root)
        }

        binding.tvTotal.text = getString(R.string.total_format, Format.rupiahComma(CartManager.checkedTotal()))
    }
}