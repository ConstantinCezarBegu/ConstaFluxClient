package com.constantin.constaflux.data.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.constantin.constaflux.data.db.entity.EntryIdTableEntity
import com.constantin.constaflux.data.db.entity.FeedEntity
import com.constantin.constaflux.data.network.response.category.CategoriesResponse
import com.constantin.constaflux.data.network.response.category.CategoryRequest
import com.constantin.constaflux.data.network.response.entry.EntriesResponse
import com.constantin.constaflux.data.network.response.entry.UpdateEntriesRequest
import com.constantin.constaflux.data.network.response.feed.CreateFeedsRequest
import com.constantin.constaflux.data.network.response.feed.UpdateFeedsRequest
import com.constantin.constaflux.data.network.response.me.MeResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import retrofit2.HttpException
import java.io.IOException
import java.lang.reflect.UndeclaredThrowableException

class MinifluxDataSource(
    private val minifluxApiServiceProvider: MinifluxApiProvider
) {

    enum class HttpErrors {
        SUCCESS,
        INTERNET_CONNECTION,
        AUTHENTICATION,
        HTTP_ERROR
    }

    //==============================================================================================
    // Error handling live data
    val error = MutableLiveData(HttpErrors.SUCCESS)

    //category live data
    private val _downloadedCategories = MutableLiveData<List<CategoriesResponse>>()
    val downloadedCategories: LiveData<List<CategoriesResponse>>
        get() = _downloadedCategories

    // feed live data
    private val _downloadedFeeds = MutableLiveData<List<FeedEntity>>()
    val downloadedFeeds: LiveData<List<FeedEntity>>
        get() = _downloadedFeeds

    // entries live data
    private val _downloadedEntries = MutableLiveData<EntriesResponse>()
    val downloadedEntries: LiveData<EntriesResponse>
        get() = _downloadedEntries

    // me live data
    private val _downloadedMe = MutableLiveData<MeResponse>()
    val downloadedMe: LiveData<MeResponse>
        get() = _downloadedMe

    //==============================================================================================
    // Category

    suspend fun fetchCategories() {
        errorHandling(true) {
            val fetchedCategories = minifluxApiServiceProvider.getApi()
                .getCategories()

            if (fetchedCategories.isNullOrEmpty()) error.postValue(HttpErrors.SUCCESS)

            _downloadedCategories.postValue(fetchedCategories)
        }
    }

    suspend fun addCategory(categoryRequest: CategoryRequest): Boolean {
        return errorHandling(true) {
            minifluxApiServiceProvider.getApi().addCategory(categoryRequest)
        }
    }

    suspend fun updateCategory(id: Long, categoryRequest: CategoryRequest): Boolean {
        return errorHandling(true) {
            minifluxApiServiceProvider.getApi().updateCategory(id, categoryRequest)
        }
    }

    suspend fun deleteCategory(id: Long): Boolean {
        return errorHandling(true) {
            minifluxApiServiceProvider.getApi().deleteCategory(id)
        }
    }

    //==============================================================================================
    // Feed
    suspend fun fetchFeeds(coroutineScope: CoroutineScope) {
        errorHandling(true) {
            val fetchedFeeds = minifluxApiServiceProvider
                .getApi().getFeeds()
            val feedEntity = fetchedFeeds.map { feed ->
                coroutineScope.async {
                    val feedIcon = try {
                        minifluxApiServiceProvider.getApi().getFeedIcon(feed.id).data
                    } catch (e: Exception) {
                        null
                    }

                    FeedEntity(
                        feed.id,
                        feed.title,
                        feed.siteUrl,
                        feed.feedUrl,
                        feed.checkedAt,
                        feed.category.id,
                        feedIcon,
                        feed.scraperRules,
                        feed.rewriteRules,
                        feed.crawler,
                        feed.username,
                        feed.password,
                        feed.userAgent
                    )
                }
            }.awaitAll()

            if (feedEntity.isNullOrEmpty()) error.postValue(HttpErrors.SUCCESS)

            _downloadedFeeds.postValue(feedEntity)
        }
    }

    suspend fun fetchFeedsEntries(clear: Boolean, status: String, feedId: Long, after: String?) {
        errorHandling(clear) {
            val fetchedFeedEntries = minifluxApiServiceProvider.getApi()
                .getFeedsEntries(status = status, id = feedId, after = after)
            fetchedFeedEntries.clear = clear
            fetchedFeedEntries.status = status
            fetchedFeedEntries.category = EntryIdTableEntity.FEED

            if (fetchedFeedEntries.entryEntities.isNullOrEmpty() && clear) {
                error.postValue(HttpErrors.SUCCESS)
            }

            _downloadedEntries.postValue(
                fetchedFeedEntries
            )
        }
    }

    suspend fun addFeed(createFeedsRequest: CreateFeedsRequest): Boolean {
        return errorHandling(true) {
            minifluxApiServiceProvider.getApi().addFeed(createFeedsRequest)
        }
    }

    suspend fun updateFeed(
        id: Long,
        updateFeedsRequest: UpdateFeedsRequest
    ): Boolean {
        return errorHandling(true) {
            minifluxApiServiceProvider.getApi().updateFeed(id, updateFeedsRequest)
        }
    }

    suspend fun deleteFeed(id: Long): Boolean {
        return errorHandling(true) {
            minifluxApiServiceProvider.getApi().deleteFeed(id)
        }
    }

    //==============================================================================================
    // Entry
    suspend fun fetchEntriesStatus(
        clear: Boolean,
        status: String?,
        starred: Boolean?,
        search: String?,
        after: String?
    ) {
        errorHandling(clear) {
            val fetchedEntries = minifluxApiServiceProvider.getApi()
                .getEntries(status = status, starred = starred, search = search, after = after)

            fetchedEntries.status = status
            fetchedEntries.clear = clear

            fetchedEntries.category =
                if (starred == null && search == null) EntryIdTableEntity.ALL
                else if (starred != null && starred == true) EntryIdTableEntity.STARRED
                else {
                    EntryIdTableEntity.SEARCH
                }

            fetchedEntries.searchQuery = search

            if (fetchedEntries.entryEntities.isNullOrEmpty() && clear) {
                error.postValue(HttpErrors.SUCCESS)
            }
            _downloadedEntries.postValue(fetchedEntries)
        }
    }

    suspend fun updateEntries(updateEntriesResponse: UpdateEntriesRequest): Boolean {
        return errorHandling(true) {
            minifluxApiServiceProvider.getApi().updateEntries(updateEntriesResponse)
        }
    }

    suspend fun updateEntriesStar(id: Long): Boolean {
        return errorHandling(true) {
            minifluxApiServiceProvider.getApi().updateEntryStar(id)
        }
    }

    //==============================================================================================
    // User
    suspend fun fetchMe() {
        errorHandling(true) {
            val fetchedMe = minifluxApiServiceProvider
                .getApi().getMe()
            _downloadedMe.postValue(fetchedMe)
        }
    }
    //==============================================================================================

    fun refreshApiService() {
        minifluxApiServiceProvider.refresh()
    }

    private suspend fun errorHandling(notifyError: Boolean, function: suspend () -> Unit): Boolean {
        return try {
            function()
            true
        } catch (e: IOException) {
            if (notifyError) error.postValue(HttpErrors.INTERNET_CONNECTION)
            false
        } catch (e: HttpException) {
            if (notifyError) when (e.code()) {
                401 -> {
                    error.postValue(HttpErrors.AUTHENTICATION)
                }
                else -> {
                    error.postValue(HttpErrors.HTTP_ERROR)
                }
            }
            false
        } catch (e: NullPointerException) {
            error.postValue(HttpErrors.SUCCESS)
            true
        } catch (e: UndeclaredThrowableException) {
            false
        }
    }
}