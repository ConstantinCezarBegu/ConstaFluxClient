package com.constantin.constaflux.data.db.dao

import androidx.room.*
import com.constantin.constaflux.data.db.entity.EntryIdTableEntity

@Dao
interface EntryIdTableDao {

    @Transaction
    fun insertContent(entryIdList: List<EntryIdTableEntity>) {
        // updateCurrent(entryIdList)
        insertNew(entryIdList)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNew(entryIdList: List<EntryIdTableEntity>)

    @Update
    fun updateCurrent(entryIdList: List<EntryIdTableEntity>)

    @Query("DELETE FROM miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 4")
    fun clearSearch()

    @Query("DELETE FROM miniflux_entry_id_table WHERE  miniflux_entry_id_table.entryTableCategoryId IN (:mode) AND miniflux_entry_id_table.entryTableEntryId IN (SELECT miniflux_entry.entryId FROM miniflux_entry WHERE miniflux_entry.entryStatus IN (:status))")
    fun clearMode(mode: Int, status: String)
}
