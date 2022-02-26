package com.hide.videophoto.data.dao

import androidx.room.*
import com.hide.videophoto.common.Constants
import com.hide.videophoto.data.entity.ActivityEntity
import io.reactivex.Single

@Dao
interface ActivityDao {

    @Query("SELECT * FROM ${Constants.DB.ACTIVITY_ENTITY}")
    fun getActivity(): Single<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createActivity(entity: ActivityEntity): Single<Long>

    @Update
    fun updateActivity(entity: ActivityEntity): Single<Int>
}