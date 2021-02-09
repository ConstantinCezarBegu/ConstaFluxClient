package com.constantin.constaflux.data.db.entity

import com.constantin.constaflux.data.converter.DisplayTime

data class EntryDisplay(
    val entryId: Long,
    val entryFeedId: Long,
    var entryStatus: String,
    val entryTitle: String,
    val entryUrl: String,
    val entryAuthor: String,
    val entryContent: String,
    var entryStarred: Boolean,
    val entryPublishedAt: DisplayTime,
    val feedTitle: String,
    val feedSiteUrl: String,
    val feedIcon: String?,
    val categoryTitle: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntryDisplay

        if (entryId != other.entryId) return false
        if (entryFeedId != other.entryFeedId) return false
        if (entryStatus != other.entryStatus) return false
        if (entryTitle != other.entryTitle) return false
        if (entryUrl != other.entryUrl) return false
        if (entryAuthor != other.entryAuthor) return false
        if (entryContent != other.entryContent) return false
        if (entryStarred != other.entryStarred) return false
        if (entryPublishedAt != other.entryPublishedAt) return false
        if (feedTitle != other.feedTitle) return false
        if (feedSiteUrl != other.feedSiteUrl) return false
        if (feedIcon != other.feedIcon) return false
        if (categoryTitle != other.categoryTitle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entryId.hashCode()
        result = 31 * result + entryFeedId.hashCode()
        result = 31 * result + entryStatus.hashCode()
        result = 31 * result + entryTitle.hashCode()
        result = 31 * result + entryUrl.hashCode()
        result = 31 * result + entryAuthor.hashCode()
        result = 31 * result + entryContent.hashCode()
        result = 31 * result + entryStarred.hashCode()
        result = 31 * result + entryPublishedAt.hashCode()
        result = 31 * result + feedTitle.hashCode()
        result = 31 * result + feedSiteUrl.hashCode()
        result = 31 * result + (feedIcon?.hashCode() ?: 0)
        result = 31 * result + categoryTitle.hashCode()
        return result
    }
}