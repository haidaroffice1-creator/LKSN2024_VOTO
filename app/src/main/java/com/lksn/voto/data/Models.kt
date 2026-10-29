package com.lksn.voto.data

import androidx.annotation.Dimension
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class CameraSummary(
    @SerializedName("id", alternate = ["Id", "ID"]) val id: Int,
    @SerializedName("name", alternate = ["Name"]) val name: String,
    @SerializedName("resolution", alternate = ["Resolution"]) val resolution: String,
    @SerializedName("price", alternate = ["Alternate"]) val price: Int,
    @SerializedName("photo", alternate = ["Photo"]) val photo: String
)

data class CameraDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sellerShop") val sellerShop: String?,
    @SerializedName("sensor") val sensor: String?,
    @SerializedName("resolution") val resolution: String?,
    @SerializedName("autoFocusSystem") val autoFocusSystem: String?,
    @SerializedName("isoRange") val isoRange: String?,
    @SerializedName("shuterSpeedRange") val shutterSpeedRange: String?,
    @SerializedName("dimensions") val dimension: String?,
    @SerializedName("weight") val weight: Int,
    @SerializedName("wiFi") val wiFi: Boolean,
    @SerializedName("touchScreen") val touchScreen: Boolean,
    @SerializedName("flash") val flash: Boolean,
    @SerializedName("bluetooth") val bluetooth: Boolean,
    @SerializedName("price") val price: Double,
    @SerializedName("photo") val photo: String?
)

data class Category(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class UserInfo(
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("address") val address: String,
    @SerializedName("votoken") val votoken: Int
)

data class OrderLine(
    @SerializedName("name") val name: String,
    @SerializedName("qty") val qty: Int,
    @SerializedName("subtotal") val subtotal: Double
)

data class Order(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String?,
    @SerializedName("transaction") val transaction: List<OrderLine>,
    @SerializedName("totalPrice") val totalPrice: Double
)

data class TransactionItemRequest(
    @SerializedName("cameraID") val cameraID: Int,
    @SerializedName("qty") val qty: Int,
    @SerializedName("subtotal") val subtotal: Double
)

data class TransactionRequest(
    @SerializedName("recipientName") val recipientName: String,
    @SerializedName("recipientPhoneNumber") val recipientPhoneNumber: String,
    @SerializedName("shippingAddress") val shippingAddress: String,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("items") val items: List<TransactionItemRequest>
)

data class CartItem(
    val cameraId: Int,
    val name: String,
    val photo: String?,
    val price: Double,
    val sellerShop: String,
    var qty: Int,
    var checked: Boolean = false
)