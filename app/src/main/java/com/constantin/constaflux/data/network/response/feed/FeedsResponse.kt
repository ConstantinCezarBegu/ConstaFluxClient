package com.constantin.constaflux.data.network.response.feed


import androidx.room.Embedded
import com.constantin.constaflux.data.network.response.category.CategoriesResponse
import com.google.gson.annotations.SerializedName

data class FeedsResponse(
    val id: Long,
    val title: String,
    @SerializedName("site_url")
    val siteUrl: String,
    @SerializedName("feed_url")
    val feedUrl: String,
    @SerializedName("checked_at")
    val checkedAt: String,
    @Embedded
    val category: CategoriesResponse,
    @Embedded(prefix = "icon_")
    val icon: FeedIconRelationResponse?,


    @SerializedName("scraper_rules")
    val scraperRules: String,
    @SerializedName("rewrite_rules")
    val rewriteRules: String,
    @SerializedName("crawler")
    val crawler: Boolean,
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("user_agent")
    val userAgent: String
)