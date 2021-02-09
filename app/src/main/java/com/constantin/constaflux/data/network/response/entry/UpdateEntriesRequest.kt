package com.constantin.constaflux.data.network.response.entry


import com.google.gson.annotations.SerializedName

data class UpdateEntriesRequest(
    @SerializedName("entry_ids")
    val entryIds: List<Long>,
    @SerializedName("status")
    val status: String
)