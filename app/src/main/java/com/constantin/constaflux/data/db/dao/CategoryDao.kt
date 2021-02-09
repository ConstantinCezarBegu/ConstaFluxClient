package com.constantin.constaflux.data.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.constantin.constaflux.data.db.entity.CategoryEntity
import com.constantin.constaflux.internal.categoryEntityExtractId

@Dao
interface CategoryDao {

    @Transaction
    fun insertContent(categoryResponseList: List<CategoryEntity>) {
        clearOld(categoryResponseList.categoryEntityExtractId())
        updateCurrent(categoryResponseList)
        insertNew(categoryResponseList)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNew(categoryResponseList: List<CategoryEntity>)

    @Update
    fun updateCurrent(categoryResponseList: List<CategoryEntity>)

    @Query("DELETE FROM miniflux_category WHERE miniflux_category.categoryId NOT IN (:id)")
    fun clearOld(id: List<Long>)

    @Query("SELECT * FROM miniflux_category")
    fun getCategories(): DataSource.Factory<Int, CategoryEntity>

    @Query("DELETE FROM miniflux_category")
    fun clearAll()
}