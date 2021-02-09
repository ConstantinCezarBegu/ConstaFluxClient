package com.constantin.constaflux.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.constantin.constaflux.data.converter.LocalDateConverter
import com.constantin.constaflux.data.db.dao.*
import com.constantin.constaflux.data.db.entity.CategoryEntity
import com.constantin.constaflux.data.db.entity.EntryEntity
import com.constantin.constaflux.data.db.entity.EntryIdTableEntity
import com.constantin.constaflux.data.db.entity.FeedEntity
import com.constantin.constaflux.data.network.response.me.MeResponse

@Database(
    entities = [CategoryEntity::class, FeedEntity::class, EntryEntity::class, EntryIdTableEntity::class, MeResponse::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class)
abstract class MinifluxDatabase : RoomDatabase() {
    abstract fun feedsDao(): FeedsDao
    abstract fun meDao(): MeDao
    abstract fun entryDao(): EntryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun entryIdTableDao(): EntryIdTableDao

    companion object {
        const val SQLITE_MAX_VARIABLE_NUMBER = 999

        @Volatile
        private var instance: MinifluxDatabase? = null
        private val LOCK = Any()

        // ?: if not initialized then it is going to initialize it
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                MinifluxDatabase::class.java, "miniflux.db"
            )
                .build()
    }
}