package com.constantin.constaflux.data.db.entity

import com.constantin.constaflux.data.converter.DisplayTime

data class Entry(
    val entryId: Long,
    var entryStatus: String,
    var entryStarred: Boolean,
    val entryTitle: String,
    val entryPublishedAt: DisplayTime,
    val feedTitle: String,
    val feedIcon: String?
) {
    fun viewEquals(otherEntry: Entry): Boolean {
        return entryId == otherEntry.entryId
                && entryStatus == otherEntry.entryStatus
                && entryStarred == otherEntry.entryStarred
                && feedIcon == otherEntry.feedIcon
                && entryTitle == otherEntry.entryTitle
                && feedTitle == otherEntry.feedTitle
                && entryPublishedAt.displayTime == otherEntry.entryPublishedAt.displayTime
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entry

        if (entryId != other.entryId) return false
        if (entryStatus != other.entryStatus) return false
        if (entryStarred != other.entryStarred) return false
        if (entryTitle != other.entryTitle) return false
        if (entryPublishedAt != other.entryPublishedAt) return false
        if (feedTitle != other.feedTitle) return false
        if (feedIcon != other.feedIcon) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entryId.hashCode()
        result = 31 * result + entryStatus.hashCode()
        result = 31 * result + entryStarred.hashCode()
        result = 31 * result + entryTitle.hashCode()
        result = 31 * result + entryPublishedAt.hashCode()
        result = 31 * result + feedTitle.hashCode()
        result = 31 * result + (feedIcon?.hashCode() ?: 0)
        return result
    }

}
