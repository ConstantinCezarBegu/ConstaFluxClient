package com.constantin.constaflux.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.constantin.constaflux.data.network.response.me.MeResponse

@Dao
interface MeDao {

    @Transaction
    fun insertContent(me: MeResponse) {
        update(me)
        insert(me)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(me: MeResponse)

    @Update
    fun update(me: MeResponse)

    @Query("SELECT * FROM miniflux_me")
    fun getMe(): LiveData<MeResponse>

    @Query("DELETE FROM miniflux_me")
    fun clearAll()
}