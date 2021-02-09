package com.constantin.constaflux.data.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.constantin.constaflux.data.db.MinifluxDatabase.Companion.SQLITE_MAX_VARIABLE_NUMBER
import com.constantin.constaflux.data.db.entity.*
import com.constantin.constaflux.data.network.response.category.CategoriesResponse
import com.constantin.constaflux.data.network.response.entry.EntriesResponse
import com.constantin.constaflux.data.network.response.me.MeResponse
import com.constantin.constaflux.internal.toCategoryEntity
import com.constantin.constaflux.internal.toEntryTableId

@Dao
interface EntryDao {

    @Transaction
    suspend fun insertAllForWorker(
        categoryDao: CategoryDao,
        feedsDao: FeedsDao,
        entryIdTableDao: EntryIdTableDao,
        meDao: MeDao,

        categoriesResponse: List<CategoriesResponse>,
        feedEntity: List<FeedEntity>,
        entriesUnreadResponse: EntriesResponse,
        entriesReadResponse: EntriesResponse,
        meResponse: MeResponse
    ) {
        categoryDao.insertContent(categoriesResponse.toCategoryEntity())
        feedsDao.insertContent(feedEntity)
        insertEntryTables(entryIdTableDao, entriesUnreadResponse)
        insertEntryTables(entryIdTableDao, entriesReadResponse)
        meDao.update(meResponse)
    }

    @Transaction
    suspend fun insertEntryTables(
        entryIdTableDao: EntryIdTableDao,
        fetchedEntryEntities: EntriesResponse
    ) {
        when {
            fetchedEntryEntities.category == EntryIdTableEntity.ALL -> {
                if (fetchedEntryEntities.clear && fetchedEntryEntities.status != null) {
                    clearAllReading(fetchedEntryEntities.status!!)
                }
                insertContent(fetchedEntryEntities.entryEntities)

                entryIdTableDao.insertContent(fetchedEntryEntities.toEntryTableId())
                fetchedEntryEntities.category = EntryIdTableEntity.STARRED
                entryIdTableDao.insertContent(fetchedEntryEntities.toEntryTableId())
                fetchedEntryEntities.category = EntryIdTableEntity.FEED
                entryIdTableDao.insertContent(fetchedEntryEntities.toEntryTableId())
            }
            fetchedEntryEntities.category == EntryIdTableEntity.SEARCH -> {
                if (fetchedEntryEntities.clear) entryIdTableDao.clearSearch()
                insertContent(fetchedEntryEntities.entryEntities)
                entryIdTableDao.insertContent(fetchedEntryEntities.toEntryTableId())
            }
            else -> {
                if (fetchedEntryEntities.clear) entryIdTableDao.clearMode(
                    fetchedEntryEntities.category,
                    fetchedEntryEntities.status!!
                )
                insertContent(fetchedEntryEntities.entryEntities)
                entryIdTableDao.insertContent(fetchedEntryEntities.toEntryTableId())
            }
        }
    }

    @Transaction
    fun insertContent(entriesResponseList: List<EntryEntity>) {
        updateCurrent(entriesResponseList)
        insertNew(entriesResponseList)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNew(entriesResponseList: List<EntryEntity>)

    @Update
    fun updateCurrent(entriesResponseList: List<EntryEntity>)

    @Transaction
    fun updateStarWrapper(id: List<Long>) {
        val chunked = id.chunked(SQLITE_MAX_VARIABLE_NUMBER - 1)
        for (chunk in chunked) updateStar(chunk)
    }

    @Transaction
    fun updateStatusWrapper(id: List<Long>, status: String) {
        val chunked = id.chunked(SQLITE_MAX_VARIABLE_NUMBER - 1)
        for (chunk in chunked) updateStatus(chunk, status)
    }

    @Query("UPDATE miniflux_entry SET entryStarred = NOT entryStarred WHERE miniflux_entry.entryId IN (:id)")
    fun updateStar(id: List<Long>)

    @Query("UPDATE miniflux_entry SET entryStatus = (:status) WHERE miniflux_entry.entryId IN (:id)")
    fun updateStatus(id: List<Long>, status: String)


    @Query("SELECT miniflux_entry.*, miniflux_feeds.feedTitle, miniflux_feeds.feedSiteUrl, miniflux_feeds.feedIcon, miniflux_category.categoryTitle FROM miniflux_entry, miniflux_feeds, miniflux_category, miniflux_entry_id_table WHERE miniflux_entry.entryId IN (:id) AND miniflux_entry.entryFeedId == miniflux_feeds.feedId AND miniflux_feeds.feedCategoryId == miniflux_category.categoryId LIMIT 1")
    fun getDisplayEntry(id: Long): EntryDisplay

    @Query("DELETE FROM miniflux_entry WHERE miniflux_entry.entryId NOT IN (:id)")
    fun clearOld(id: List<Long>)

    @Query("SELECT entryPublishedAt FROM miniflux_entry ORDER BY entryPublishedAt DESC LIMIT 1")
    fun getLatestEntry(): String?

    @Query("SELECT miniflux_entry.entryId, miniflux_entry.entryStatus, miniflux_entry.entryStarred, miniflux_entry.entryTitle, miniflux_entry.entryPublishedAt, miniflux_feeds.feedTitle, miniflux_feeds.feedIcon FROM miniflux_entry, miniflux_feeds, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 1 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryStatus IN (:status) AND miniflux_entry.entryFeedId == miniflux_feeds.feedId  ORDER BY entryPublishedAt DESC")
    fun getEntriesAll(status: String): DataSource.Factory<Int, Entry>

    @Query("SELECT miniflux_entry.entryId, miniflux_entry.entryStatus, miniflux_entry.entryStarred, miniflux_entry.entryTitle, miniflux_entry.entryPublishedAt, miniflux_feeds.feedTitle, miniflux_feeds.feedIcon FROM miniflux_entry, miniflux_feeds, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 2 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryStatus IN (:status) AND miniflux_entry.entryStarred == 1 AND miniflux_entry.entryFeedId == miniflux_feeds.feedId ORDER BY entryPublishedAt DESC")
    fun getEntriesStarred(status: String): DataSource.Factory<Int, Entry>

    @Query("SELECT miniflux_entry.entryId, miniflux_entry.entryStatus, miniflux_entry.entryStarred, miniflux_entry.entryTitle, miniflux_entry.entryPublishedAt, miniflux_feeds.feedTitle, miniflux_feeds.feedIcon FROM miniflux_entry, miniflux_feeds, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 3 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryStatus IN (:status) AND miniflux_entry.entryFeedId IN (:feedId) AND miniflux_entry.entryFeedId == miniflux_feeds.feedId ORDER BY entryPublishedAt DESC")
    fun getFeedEntries(status: String, feedId: Long): DataSource.Factory<Int, Entry>

    @Query("SELECT miniflux_entry.entryId, miniflux_entry.entryStatus, miniflux_entry.entryStarred, miniflux_entry.entryTitle, miniflux_entry.entryPublishedAt, miniflux_feeds.feedTitle, miniflux_feeds.feedIcon FROM miniflux_entry, miniflux_feeds, miniflux_category, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 1 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryStatus IN (:status) AND miniflux_entry.entryFeedId == miniflux_feeds.feedId AND miniflux_feeds.feedCategoryId == miniflux_category.categoryId AND miniflux_category.categoryId IN (:categoryId) ORDER BY entryPublishedAt DESC")
    fun getCategoryEntries(
        status: String,
        categoryId: Long
    ): DataSource.Factory<Int, Entry>

    @Query("SELECT miniflux_entry.entryId FROM miniflux_entry, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 1 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryStatus IN (:status) ORDER BY entryPublishedAt DESC")
    fun getEntriesAllALLID(status: String): LiveData<List<Long>>

    @Query("SELECT miniflux_entry.entryId FROM miniflux_entry, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 2 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryStatus IN (:status) AND miniflux_entry.entryStarred == 1 ORDER BY entryPublishedAt DESC")
    fun getEntriesStarredALLID(status: String): LiveData<List<Long>>

    @Query("SELECT miniflux_entry.entryId FROM miniflux_entry, MINIFLUX_FEEDS, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 3 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryStatus IN (:status) AND miniflux_entry.entryFeedId IN (:feedId) AND miniflux_entry.entryFeedId == miniflux_feeds.feedId ORDER BY entryPublishedAt DESC")
    fun getFeedEntriesALLID(status: String, feedId: Long): LiveData<List<Long>>

    @Query("SELECT miniflux_entry.entryId FROM miniflux_entry, miniflux_feeds, miniflux_category, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 1 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryStatus IN (:status) AND miniflux_entry.entryFeedId == miniflux_feeds.feedId AND miniflux_feeds.feedCategoryId == miniflux_category.categoryId AND miniflux_category.categoryId IN (:categoryId) ORDER BY entryPublishedAt DESC")
    fun getCategoryEntriesALLID(
        status: String,
        categoryId: Long
    ): LiveData<List<Long>>

    @Query("SELECT miniflux_entry.entryId, miniflux_entry.entryStatus, miniflux_entry.entryStarred, miniflux_entry.entryTitle, miniflux_entry.entryPublishedAt, miniflux_feeds.feedTitle, miniflux_feeds.feedIcon FROM miniflux_entry, miniflux_feeds, miniflux_category, miniflux_entry_id_table WHERE miniflux_entry_id_table.entryTableCategoryId = 4 AND miniflux_entry_id_table.entryTableEntryId = miniflux_entry.entryId AND miniflux_entry.entryFeedId == miniflux_feeds.feedId AND miniflux_feeds.feedCategoryId == miniflux_category.categoryId ORDER BY entryPublishedAt DESC")
    fun getEntriesSearch(): DataSource.Factory<Int, Entry>

    @Query("DELETE FROM miniflux_entry")
    fun clearAll()

    @Query("DELETE FROM miniflux_entry WHERE miniflux_entry.entryStatus IN (:status)")
    fun clearAllReading(status: String)

}