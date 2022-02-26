package com.hide.videophoto.data.dao

import androidx.room.*
import com.hide.videophoto.common.Constants
import com.hide.videophoto.data.entity.AuthEntity
import io.reactivex.Single

@Dao
interface AuthDao {

    @Query("SELECT * FROM ${Constants.DB.AUTH_ENTITY} WHERE ${Constants.DB.PASSWORD} LIKE :password")
    fun login(password: String): Single<AuthEntity>

    @Query("SELECT ${Constants.DB.ROWID}, ${Constants.DB.PASSWORD_FAKE}, ${Constants.DB.SECURITY_QUESTION}, ${Constants.DB.SECURITY_ANSWER}, ${Constants.DB.EMAIL} FROM ${Constants.DB.AUTH_ENTITY} LIMIT 1")
    fun getBasicUserInfo(): Single<AuthEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createUser(entity: AuthEntity): Single<Long>

    @Update
    fun updateUser(entity: AuthEntity): Single<Int>

    @Query("SELECT COUNT(*) FROM ${Constants.DB.AUTH_ENTITY}")
    fun countUser(): Single<Int>
}