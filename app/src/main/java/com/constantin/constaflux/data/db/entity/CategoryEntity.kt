package com.constantin.constaflux.data.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "miniflux_category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = false)
    val categoryId: Long,
    val categoryTitle: String
) : Parcelable {
    fun viewEquals(otherCategory: CategoryEntity): Boolean {
        return this.categoryId == otherCategory.categoryId
                && this.categoryTitle == otherCategory.categoryTitle
    }
}