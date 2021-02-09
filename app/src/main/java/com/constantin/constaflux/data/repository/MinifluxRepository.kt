package com.constantin.constaflux.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.constantin.constaflux.data.db.dao.*
import com.constantin.constaflux.data.db.entity.*
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.network.MinifluxDataSource
import com.constantin.constaflux.data.network.response.category.CategoryRequest
import com.constantin.constaflux.data.network.response.entry.EntriesResponse
import com.constantin.constaflux.data.network.response.entry.UpdateEntriesRequest
import com.constantin.constaflux.data.network.response.feed.CreateFeedsRequest
import com.constantin.constaflux.data.network.response.feed.UpdateFeedsRequest
import com.constantin.constaflux.data.network.response.me.MeResponse
import com.constantin.constaflux.internal.toCategoryEntity
import kotlinx.coroutines.*

class MinifluxRepository(
    private val feedsDao: FeedsDao,
    private val dataSource: MinifluxDataSource,
    private val meDao: MeDao,
    private val entryDao: EntryDao,
    private val entryIdTableEntityDao: EntryIdTableDao,
    private val categoryDao: CategoryDao,
    private val encrypt: UserEncrypt
) {

    init {
        dataSource.downloadedCategories.observeForever { categoriesEntriesResponse ->
            persistFetchedCategories(categoriesEntriesResponse.toCategoryEntity())
        }
        dataSource.downloadedFeeds.observeForever { newFeeds ->
            persistFetchedFeeds(newFeeds)
        }
        dataSource.downloadedEntries.observeForever { entriesResponse ->
            if (entriesResponse.category == EntryIdTableEntity.SEARCH) {
                encrypt.saveSearch(entriesResponse.searchQuery!!)
            }
            persistFetchedEntries(entriesResponse)
        }

        dataSource.downloadedMe.observeForever { newMe ->
            persistFetchedMe(newMe)
        }
    }

    val errorLiveData = dataSource.error

    //==============================================================================================
    // These are the repository functions that are related to the Categories.
    suspend fun getCategories(): LiveData<PagedList<CategoryEntity>> {
        return withContext(Dispatchers.IO) {
            return@withContext categoryDao.getCategories().toLiveData(pageSize = 100)
        }
    }

    suspend fun fetchCategories() {
        dataSource.fetchCategories()
    }

    suspend fun addCategory(categoryRequest: CategoryRequest): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.addCategory(categoryRequest)
        }
    }

    suspend fun updateCategory(id: Long, categoryRequest: CategoryRequest): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.updateCategory(id, categoryRequest)
        }
    }

    suspend fun deleteCategory(id: Long): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.deleteCategory(id)
        }
    }

    private fun persistFetchedCategories(fetchedCategories: List<CategoryEntity>) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insertContent(fetchedCategories)
        }
    }

    // =============================================================================================
    // These are the repository functions that are related to the feeds.
    suspend fun getFeeds(): LiveData<PagedList<Feed>> {
        return withContext(Dispatchers.IO) {
            return@withContext feedsDao.getFeeds().toLiveData(pageSize = 100)
        }
    }

    suspend fun fetchFeeds(coroutineScope: CoroutineScope) {
        dataSource.fetchFeeds(coroutineScope)
    }

    suspend fun getFeedEntries(
        clear: Boolean,
        status: String,
        feedId: Long
    ): DataSource.Factory<Int, Entry> {
        fetchFeedEntries(clear, status, feedId, null)
        return withContext(Dispatchers.IO) {
            return@withContext entryDao.getFeedEntries(status, feedId)
        }
    }

    suspend fun getFeedEntriesALLID(
        status: String,
        feedId: Long
    ): LiveData<List<Long>> {
        return withContext(Dispatchers.IO) {
            return@withContext entryDao.getFeedEntriesALLID(status, feedId)
        }
    }

    suspend fun fetchFeedEntries(clear: Boolean, status: String, feedId: Long, after: String?) {
        dataSource.fetchFeedsEntries(clear, status, feedId, after)
    }

    suspend fun addFeed(createFeedsRequest: CreateFeedsRequest): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.addFeed(createFeedsRequest)
        }
    }

    suspend fun updateFeed(
        id: Long,
        updateFeedsRequest: UpdateFeedsRequest
    ): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.updateFeed(id, updateFeedsRequest)
        }
    }

    suspend fun deleteFeed(id: Long): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.deleteFeed(id)
        }
    }

    private fun persistFetchedFeeds(fetchedFeeds: List<FeedEntity>) {
        GlobalScope.launch(Dispatchers.IO) {
            errorHandlingDatabase(false, this) {
                feedsDao.insertContent(fetchedFeeds)
            }
        }
    }

    //==============================================================================================
    // This is for the unread entryEntities request.
    // status can be read or unread
    suspend fun getSearchedEntries(clear: Boolean, search: String): DataSource.Factory<Int, Entry> {
        fetchEntriesStatus(clear, null, null, search, null)
        return withContext(Dispatchers.IO) {
            entryDao.getEntriesSearch()
        }
    }

    fun getEntryDisplay(id: Long): EntryDisplay {
        return runBlocking(Dispatchers.IO) {
            entryDao.getDisplayEntry(id)
        }
    }

    suspend fun getEntries(
        clear: Boolean,
        status: String,
        starred: Boolean?
    ): DataSource.Factory<Int, Entry> {
        fetchEntriesStatus(clear, status, starred, null, null)
        return withContext(Dispatchers.IO) {
            if (starred == null) {
                entryDao.getEntriesAll(status)
            } else {
                entryDao.getEntriesStarred(status)
            }
        }
    }

    suspend fun getEntriesALLID(
        status: String,
        starred: Boolean?
    ): LiveData<List<Long>> {
        return withContext(Dispatchers.IO) {
            if (starred == null) {
                entryDao.getEntriesAllALLID(status)
            } else {
                entryDao.getEntriesStarredALLID(status)
            }
        }
    }

    suspend fun getCategoryEntries(
        clear: Boolean,
        status: String,
        categoryId: Long
    ): DataSource.Factory<Int, Entry> {
        fetchEntriesStatus(clear, status, null, null, null)
        return withContext(Dispatchers.IO) {
            return@withContext entryDao.getCategoryEntries(status, categoryId)
        }
    }

    suspend fun getCategoryEntriesALLID(
        status: String,
        categoryId: Long
    ): LiveData<List<Long>> {
        return withContext(Dispatchers.IO) {
            return@withContext entryDao.getCategoryEntriesALLID(status, categoryId)
        }
    }

    suspend fun fetchEntriesStatus(
        clear: Boolean,
        status: String?,
        starred: Boolean?,
        search: String?,
        after: String?
    ) {
        dataSource.fetchEntriesStatus(
            clear = clear,
            status = status,
            starred = starred,
            search = search,
            after = after
        )
    }

    private fun persistFetchedEntries(
        fetchedEntryEntities: EntriesResponse
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            errorHandlingDatabase(true, this) {
                entryDao.insertEntryTables(entryIdTableEntityDao, fetchedEntryEntities)
            }
        }
    }

    suspend fun updateEntries(updateEntriesResponse: UpdateEntriesRequest): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.updateEntries(updateEntriesResponse)
        }
    }

    suspend fun updateEntryStar(id: Long): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext dataSource.updateEntriesStar(id)
        }
    }

//    fun updateEntriesDatabase(entries: List<Long>) {
//        GlobalScope.launch {
//            entryDao.updateCurrent(entries)
//        }
//    }

    fun updateEntryStar(entries: List<Long>) {
        GlobalScope.launch {
            entryDao.updateStarWrapper(entries)
        }
    }

    fun updateEntryStatus(entries: List<Long>, status: String) {
        GlobalScope.launch {
            entryDao.updateStatusWrapper(entries, status)
        }
    }


    //==============================================================================================
    // This is used clearing the entries data in the settings fragment.
    fun clearEntries() {
        GlobalScope.launch {
            entryDao.clearAll()
        }
    }

    fun clearAll() {
        GlobalScope.launch {
            categoryDao.clearAll()
            feedsDao.clearAll()
            entryDao.clearAll()
        }
    }

    //==============================================================================================
    // This is for the me request.
    suspend fun getMe(): LiveData<MeResponse> {
        return withContext(Dispatchers.IO) {
            return@withContext meDao.getMe()
        }
    }


    private fun persistFetchedMe(fetchedMe: MeResponse) {
        GlobalScope.launch(Dispatchers.IO) {
            meDao.insertContent(fetchedMe)
        }
    }

    private suspend fun initMe() {
        // here you can add logic for when you want or don't want to fetch the feeds
        fetchMe()
    }

    suspend fun fetchMe() {
        dataSource.fetchMe()
    }

    //==============================================================================================

    fun refreshApiService() {
        dataSource.refreshApiService()
    }

    private suspend fun errorHandlingDatabase(
        isRetry: Boolean,
        coroutineScope: CoroutineScope,
        function: suspend () -> Unit
    ) {
        try {
            function()
        } catch (e: SQLiteConstraintException) {
            fetchCategories()
            fetchFeeds(coroutineScope)
            if (isRetry) {
                function()
            }
        }
    }
}