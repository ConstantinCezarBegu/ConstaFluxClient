package com.constantin.constaflux.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "miniflux_feeds",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class, parentColumns = ["categoryId"],
        childColumns = ["feedCategoryId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["feedCategoryId", "feedId"])]
)

data class FeedEntity(
    @PrimaryKey(autoGenerate = false)
    val feedId: Long,
    val feedTitle: String,
    val feedSiteUrl: String,
    val feedUrl: String,
    val feedCheckedAt: String,
    val feedCategoryId: Long,
    val feedIcon: String?,

    val feedScraperRules: String,
    val feedRewriteRules: String,
    val feedCrawler: Boolean,
    val feedUsername: String,
    val feedPassword: String,
    val feedUserAgent: String
)