package com.constantin.constaflux.data.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.constantin.constaflux.data.db.entity.Feed
import com.constantin.constaflux.data.db.entity.FeedEntity
import com.constantin.constaflux.internal.feedEntityExtractId

@Dao
interface FeedsDao {

    @Transaction
    fun insertContent(feedsResponseList: List<FeedEntity>) {
        clearOld(feedsResponseList.feedEntityExtractId())
        updateCurrent(feedsResponseList)
        insertNew(feedsResponseList)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNew(feedsResponseList: List<FeedEntity>)

    @Update
    fun updateCurrent(feedsResponseList: List<FeedEntity>)

    @Query("DELETE FROM miniflux_feeds WHERE miniflux_feeds.feedId NOT IN (:id)")
    fun clearOld(id: List<Long>)

    @Query("SELECT miniflux_feeds.*, miniflux_category.categoryTitle FROM miniflux_feeds, miniflux_category WHERE  miniflux_feeds.feedCategoryId == miniflux_category.categoryId")
    fun getFeeds(): DataSource.Factory<Int, Feed>

    @Query("DELETE FROM miniflux_feeds")
    fun clearAll()
}