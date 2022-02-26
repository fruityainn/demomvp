package com.hide.videophoto.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hide.videophoto.common.Constants

@Entity(tableName = Constants.DB.AUTH_ENTITY)
class AuthEntity : BaseEntity() {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.ROWID)
    var rowId: Long? = null

    @ColumnInfo(name = Constants.DB.PASSWORD)
    var password: String? = null

    @ColumnInfo(name = Constants.DB.PASSWORD_FAKE)
    var passwordFake: String? = null

    @ColumnInfo(name = Constants.DB.EMAIL)
    var email: String? = null

    @ColumnInfo(name = Constants.DB.SECURITY_QUESTION)
    var securityQuestion: String? = null

    @ColumnInfo(name = Constants.DB.SECURITY_ANSWER)
    var securityAnswer: String? = null
}