package com.constantin.constaflux.data.network.response.category


import com.google.gson.annotations.SerializedName

data class CategoryRequest(
    @SerializedName("title")
    val title: String
)