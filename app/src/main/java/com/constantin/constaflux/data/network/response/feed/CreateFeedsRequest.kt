package com.constantin.constaflux.data.network.response.feed


import com.google.gson.annotations.SerializedName

data class CreateFeedsRequest(
    @SerializedName("feed_url")
    val feedUrl: String,
    @SerializedName("category_id")
    val categoryId: Long,

    // Optional fields
    val username: String?,
    val password: String?,
    val crawler: Boolean,
    @SerializedName("user_agent")
    val userAgent: String?
)