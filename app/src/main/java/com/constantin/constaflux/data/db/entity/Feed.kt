package com.constantin.constaflux.data.db.entity

import android.os.Parcelable
import com.constantin.constaflux.data.converter.DisplayTime
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Feed(
    val feedId: Long,
    val feedTitle: String,
    val feedSiteUrl: String,
    val feedUrl: String,
    val feedCheckedAt: DisplayTime,
    val feedCategoryId: Long,
    val feedIcon: String?,

    val feedScraperRules: String,
    val feedRewriteRules: String,
    val feedCrawler: Boolean,
    val feedUsername: String,
    val feedPassword: String,
    val feedUserAgent: String,

    val categoryTitle: String
) : Parcelable {
    fun viewEquals(otherFeed: Feed): Boolean {
        return feedId == otherFeed.feedId
                && feedIcon == otherFeed.feedIcon
                && feedTitle == otherFeed.feedTitle
                && categoryTitle == otherFeed.categoryTitle
                && feedCheckedAt.displayTime == otherFeed.feedCheckedAt.displayTime
    }
}