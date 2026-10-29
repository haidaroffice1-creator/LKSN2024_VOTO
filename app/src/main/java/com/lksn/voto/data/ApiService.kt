package com.lksn.voto.data

import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth")
    suspend fun login(@Body body: AuthRequest): Response<ResponseBody>

    @GET("camera")
    suspend fun getCameras(
        @Query("categoryID") categoryId: Int?,
        @Query("search") search: String?
    ): Response<List<CameraSummary>>

    @GET("camera/{id}")
    suspend fun getCamera(@Path("id") id: Int): Response<CameraDetail>

    @GET("category")
    suspend fun getCategories(): Response<List<Category>>

    @GET("me")
    suspend fun  getMe(): Response<UserInfo>

    @GET("me/transaction")
    suspend fun getMyTransaction(): Response<List<Order>>

    @POST("transaction")
    suspend fun addTransaction(@Body body: TransactionRequest): Response<ResponseBody>

    @PUT("me/votoken/update")
    suspend fun updateVotoken(@Query("amount") amount: Int): Response<ResponseBody>

}