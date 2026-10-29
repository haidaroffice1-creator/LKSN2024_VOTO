package com.lksn.voto.data

object Session {
    @Volatile
    var token: String? = null
    var user: UserInfo? = null

    val isLoggedIn get() = token != null
    fun clear() {
        token = null
        user = null
    }
}