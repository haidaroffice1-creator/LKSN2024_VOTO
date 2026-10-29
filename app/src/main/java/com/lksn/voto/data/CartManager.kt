package com.lksn.voto.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private const val PREFS = "voto_chart"
    private val gson = Gson()

    private var appContext: Context? = null
    private var userKey: String = ""
    private val items = mutableListOf<CartItem>()

    fun load(context: Context, UserKey: String) {
        appContext = context.applicationContext
        this.userKey = userKey
        items.clear()
        val json = prefs()?.getString(key(), null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<CartItem>>() {}.type
                val saved: List<CartItem>? = gson.fromJson(json, type)
                saved?.let { items.addAll(it) }
            } catch (e: Exception) {
                items.clear()
            }
        }
    }

    fun unload() {
        items.clear()
        userKey = ""
    }

    fun getItems(): List<CartItem> = items.toList()
    fun getShops(): List<String> = items.map { it.sellerShop }.distinct()
    fun itemsOf(shop: String): List<CartItem> = items.filter { it.sellerShop == shop }
    fun add(camera: CameraDetail) {
        val existing = items.firstOrNull { it.cameraId == camera.id }
        if (existing != null) {
            existing.qty += 1
        } else {
            items.add(
                CartItem(
                    cameraId = camera.id,
                    name = camera.name,
                    photo = camera.photo,
                    price = camera.price,
                    sellerShop = camera.sellerShop.orEmpty(),
                    qty = 1
                )
            )
        }
        save()
    }

    fun increase(cameraId: Int) {
        items.firstOrNull { it.cameraId == cameraId }?.let { it.qty += 1 }
        save()
    }

    fun decrease(cameraId: Int) {
        val item = items.firstOrNull { it.cameraId == cameraId } ?: return
        item.qty -= 1
        if (item.qty <= 0) items.remove(item)
        save()
    }

    fun setChecked(cameraId: Int, checked: Boolean) {
        items.firstOrNull {it.cameraId == cameraId }?.checked = checked
        save()
    }

    fun setShopChecked(shop: String, checked: Boolean) {
        items.filter { it.sellerShop == shop }.forEach { it.checked = checked }
        save()
    }

    fun isShopChecked(shop: String): Boolean {
        val list = itemsOf(shop)
        return list.isNotEmpty() && list.all { it.checked }
    }

    fun getCheckedItems(): List<CartItem> = items.filter { it.checked }
    fun checkedTotal(): Double = getCheckedItems().sumOf { it.price * it.qty }
    fun removeChecked() {
        items.removeAll { it.checked }
        save()
    }

    private fun key() = "cart_$userKey"
    private fun prefs() = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun save() {
        if (userKey.isEmpty()) return
        prefs()?.edit()?.putString(key(), gson.toJson(items))?.apply()
    }
}