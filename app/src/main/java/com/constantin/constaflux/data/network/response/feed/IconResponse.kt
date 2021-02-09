package com.constantin.constaflux.data.network.response.feed


import com.google.gson.annotations.SerializedName


data class IconResponse(
    val `data`: String,
    val id: Long,
    @SerializedName("mime_type")
    val mimeType: String
)