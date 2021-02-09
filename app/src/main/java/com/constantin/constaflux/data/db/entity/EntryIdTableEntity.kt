package com.constantin.constaflux.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "miniflux_entry_id_table",
    foreignKeys = [ForeignKey(
        entity = EntryEntity::class, parentColumns = ["entryId"],
        childColumns = ["entryTableEntryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["entryTableEntryId", "entryTableCategoryId"])],
    primaryKeys = ["entryTableEntryId", "entryTableCategoryId"]
)
data class EntryIdTableEntity(
    val entryTableEntryId: Long,
    val entryTableCategoryId: Int
) {
    companion object {
        const val ALL = 1
        const val STARRED = 2
        const val FEED = 3
        const val SEARCH = 4
    }
}