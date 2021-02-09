package com.constantin.constaflux.data.network

import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.network.response.ErrorResponse
import com.constantin.constaflux.data.network.response.category.CategoriesResponse
import com.constantin.constaflux.data.network.response.category.CategoryRequest
import com.constantin.constaflux.data.network.response.entry.EntriesResponse
import com.constantin.constaflux.data.network.response.entry.UpdateEntriesRequest
import com.constantin.constaflux.data.network.response.feed.CreateFeedsRequest
import com.constantin.constaflux.data.network.response.feed.FeedsResponse
import com.constantin.constaflux.data.network.response.feed.IconResponse
import com.constantin.constaflux.data.network.response.feed.UpdateFeedsRequest
import com.constantin.constaflux.data.network.response.me.MeResponse
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface MinifluxApiService {
    //==============================================================================================
    // Category
    @GET("categories")
    suspend fun getCategories(): List<CategoriesResponse>

    @POST("categories")
    suspend fun addCategory(
        @Body categoryRequest: CategoryRequest
    ): ErrorResponse

    @PUT("categories/{categoryId}")
    suspend fun updateCategory(
        @Path("categoryId") id: Long,
        @Body categoryRequest: CategoryRequest
    ): ErrorResponse

    @DELETE("categories/{categoryId}")
    suspend fun deleteCategory(
        @Path("categoryId") id: Long
    ): ErrorResponse

    //==============================================================================================
    // Feed
    @GET("feeds")
    suspend fun getFeeds(): List<FeedsResponse>

    @POST("feeds")
    suspend fun addFeed(
        @Body createFeedsRequest: CreateFeedsRequest
    ): ErrorResponse

    @PUT("feeds/{feedId}")
    suspend fun updateFeed(
        @Path("feedId") id: Long,
        @Body updateFeedsRequest: UpdateFeedsRequest
    ): ErrorResponse

    @DELETE("feeds/{feedId}")
    suspend fun deleteFeed(
        @Path("feedId") id: Long
    ): ErrorResponse

    //GET /v1/feeds/106/icon
    @GET("feeds/{selectedId}/icon")
    suspend fun getFeedIcon(
        @Path("selectedId") id: Long
    ): IconResponse

    //GET /v1/feeds/42/entries?limit=1&order=id&direction=asc
    @GET("feeds/{selectedId}/entries")
    suspend fun getFeedsEntries(
        @Path("selectedId") id: Long,
        @Query("status") status: String,
        @Query("direction") direction: String = "desc",
        @Query("before") after: String? = null
    ): EntriesResponse

    //==============================================================================================
    // Entry
    @GET("entries")
    suspend fun getEntries(
        @Query("status") status: String?,
        @Query("direction") direction: String = "desc",
        @Query("starred") starred: Boolean?,
        @Query("search") search: String?,
        @Query("before") after: String?
    ): EntriesResponse

    //PUT /v1/entries
    @PUT("entries")
    suspend fun updateEntries(
        @Body updateEntriesResponse: UpdateEntriesRequest
    ): ErrorResponse

    @PUT("entries/{entryId}/bookmark")
    suspend fun updateEntryStar(
        @Path("entryId") id: Long
    ): ErrorResponse

    //==============================================================================================
    //User info
    @GET("me")
    suspend fun getMe(): MeResponse
    //==============================================================================================


    companion object {
        operator fun invoke(
            userEncrypt: UserEncrypt
        ): MinifluxApiService {
            val user = userEncrypt.getUser()

            val requestInterceptor = Interceptor { chain ->

                val request = chain.request()
                    .newBuilder()
                    .header(
                        "Authorization",
                        Credentials.basic(user.username, user.password)
                    )
                    .build()

                return@Interceptor chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(requestInterceptor)
                .build()
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("${user.url}/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MinifluxApiService::class.java)
        }
    }
}