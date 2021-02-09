package com.constantin.constaflux.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(
    tableName = "miniflux_entry",
    foreignKeys = [ForeignKey(
        entity = FeedEntity::class, parentColumns = ["feedId"],
        childColumns = ["entryFeedId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["entryFeedId", "entryId"])]
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id")
    val entryId: Long,
    @SerializedName("feed_id")
    val entryFeedId: Long,
    @SerializedName("title")
    val entryTitle: String,
    @SerializedName("url")
    val entryUrl: String,
    @SerializedName("author")
    val entryAuthor: String,
    @SerializedName("content")
    val entryContent: String,
    @SerializedName("published_at")
    val entryPublishedAt: String,
    @SerializedName("status")
    val entryStatus: String,
    @SerializedName("starred")
    val entryStarred: Boolean
)