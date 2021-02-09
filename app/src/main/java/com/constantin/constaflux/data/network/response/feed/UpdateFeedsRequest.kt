package com.constantin.constaflux.data.network.response.feed


import com.google.gson.annotations.SerializedName

data class UpdateFeedsRequest(
    @SerializedName("feed_url")
    val feedUrl: String,
    @SerializedName("site_url")
    val siteUrl: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("category_id")
    val categoryId: Long,
    @SerializedName("scraper_rules")
    val scraperRules: String?,
    @SerializedName("rewrite_rules")
    val rewriteRules: String?,
    @SerializedName("crawler")
    val crawler: Boolean,
    @SerializedName("username")
    val username: String?,
    @SerializedName("password")
    val password: String?,
    @SerializedName("user_agent")
    val userAgent: String?
)