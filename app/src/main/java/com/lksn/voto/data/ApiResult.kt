package com.lksn.voto.data

import android.content.Context
import android.os.Message
import com.lksn.voto.R
import okio.IOException
import retrofit2.Response

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val code: Int, val message: String) : ApiResult<Nothing>() {
        val isUnauthorized get() = code == 401
    }
}

suspend fun <T> apiCall(context: Context, block: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            val body = response.body() as T
            ApiResult.Success(body)
        } else {
            val raw = try {
                response.errorBody()?.string()?.trim()?.trim('"').orEmpty()
            } catch (e: Exception) {
                ""
            }
            val message = when(response.code()) {
                401 -> context.getString(R.string.err_session_expired)
                else -> raw.ifBlank {context.getString(R.string.err_unknown)}
            }
            ApiResult.Failure(response.code(), message)
        }
    } catch (e: IOException) {
        ApiResult.Failure(-1, context.getString(R.string.err_network))
    } catch (e: Exception) {
        ApiResult.Failure(-2, context.getString(R.string.err_unknown))
    }
}