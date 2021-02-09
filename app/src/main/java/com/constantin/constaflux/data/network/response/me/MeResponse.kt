package com.constantin.constaflux.data.network.response.me


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "miniflux_me")
data class MeResponse(
    @SerializedName("entry_sorting_direction")
    val entrySortingDirection: String,
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    @SerializedName("is_admin")
    val isAdmin: Boolean,
    val language: String,
    @SerializedName("last_login_at")
    val lastLoginAt: String,
    val theme: String,
    val timezone: String,
    val username: String
)