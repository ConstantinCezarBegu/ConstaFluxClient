package com.constantin.constaflux.data.network.response.feed

import com.google.gson.annotations.SerializedName

data class FeedIconRelationResponse(
    @SerializedName("feed_id")
    val feedId: Long,
    @SerializedName("icon_id")
    val iconId: Long
)