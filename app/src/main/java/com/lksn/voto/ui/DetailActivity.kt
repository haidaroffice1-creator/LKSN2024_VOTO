package com.lksn.voto.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.lksn.voto.R
import com.lksn.voto.data.ApiClient
import com.lksn.voto.data.ApiResult
import com.lksn.voto.data.CameraDetail
import com.lksn.voto.data.CartManager
import com.lksn.voto.data.apiCall
import com.lksn.voto.databinding.ActivityDetailBinding
import com.lksn.voto.databinding.ItemSpecBinding
import com.lksn.voto.util.Format
import com.lksn.voto.util.loadPhoto
import com.lksn.voto.util.toYesNo
import kotlinx.coroutines.launch


class DetailActivity : BaseActivity() {
    companion object {
        const val EXTRA_CAMERA_ID = "extra_camera_id"
    }

    private lateinit var binding: ActivityDetailBinding
    private var camera: CameraDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddToCart.isEnabled = false
        binding.btnAddToCart.setOnClickListener { addToCart() }

        val id = intent.getIntExtra(EXTRA_CAMERA_ID, -1)
        if (id < 0) {
            finish()
            return
        }
        loadDetail(id)
    }

    private fun loadDetail(id: Int) {
        lifecycleScope.launch {
            when (val result = apiCall(this@DetailActivity) {
                ApiClient.api.getCamera(id) }) {
                is ApiResult.Success -> {
                    camera = result.data
                    bind(result.data)
                    binding.btnAddToCart.isEnabled = true
                }
                is ApiResult.Failure -> showFailure(result)
            }
        }
    }

    private fun bind(c: CameraDetail) {
        binding.ivPhoto.loadPhoto(c.photo)
        binding.tvName.text = c.name
        binding.tvSeller.text = c.sellerShop.orEmpty()
        binding.tvPrice.text = Format.rupiahComma(c.price)

        val specs = listOf(
            R.string.sensor to c.sensor.orEmpty(),
            R.string.resolution to c.resolution.orEmpty(),
            R.string.autofocus to c.autoFocusSystem.orEmpty(),
            R.string.iso_range to c.isoRange.orEmpty(),
            R.string.shutter_speed to c.shutterSpeedRange.orEmpty(),
            R.string.dimensions to c.dimension.orEmpty(),
            R.string.weight to "${c.weight}g",
            R.string.wifi to c.wiFi.toYesNo(this),
            R.string.touch_screen to c.touchScreen.toYesNo(this),
            R.string.flash to c.flash.toYesNo(this),
            R.string.bluetooth to c.bluetooth.toYesNo(this)
        )
        val columns = listOf(binding.specCol1, binding.specCol2, binding.specCol3)
        columns.forEach { it.removeAllViews() }
        specs.forEachIndexed { index, (label, value) ->
            val column = columns[index % columns.size]
            val item = ItemSpecBinding.inflate(layoutInflater, column, false)
            item.tvLabel.setText(label)
            item.tvValue.text = value
            column.addView(item.root)
        }
    }

    private fun addToCart() {
        val c = camera ?: return
        CartManager.add(c)
        startActivity(Intent(this, CartActivity::class.java))
    }
}