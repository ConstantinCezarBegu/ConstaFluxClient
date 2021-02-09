package com.constantin.constaflux.data.network.response.entry


import com.constantin.constaflux.data.db.entity.EntryEntity
import com.google.gson.annotations.SerializedName

data class EntriesResponse(
    @SerializedName("entries")
    val entryEntities: List<EntryEntity>,
    @SerializedName("total")
    val total: Long,

    var status: String?,
    var clear: Boolean,
    var category: Int = 0,
    var searchQuery: String? = null
)