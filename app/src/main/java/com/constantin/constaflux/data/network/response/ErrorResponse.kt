package com.constantin.constaflux.data.network.response


import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error_message")
    val errorMessage: String
)