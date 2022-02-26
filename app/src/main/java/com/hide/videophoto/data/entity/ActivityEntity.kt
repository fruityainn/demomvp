package com.hide.videophoto.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hide.videophoto.common.Constants

@Entity(tableName = Constants.DB.ACTIVITY_ENTITY)
class ActivityEntity : BaseEntity() {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.ROWID)
    var rowId: Long? = null

    @ColumnInfo(name = Constants.DB.INSTALLED_TIME)
    var installedTime: Int? = null

    @ColumnInfo(name = Constants.DB.LAST_LOGGED_IN)
    var lastLogin: Int? = null

    @ColumnInfo(name = Constants.DB.LAST_ADDED_CONTENT)
    var lastAddedContent: String? = null

    @ColumnInfo(name = Constants.DB.FAILED_RESET_TIMES)
    var failedResetTimes: Int = 0

    @ColumnInfo(name = Constants.DB.LAST_TIME_AD_CLICKED)
    var lastTimeAdClicked: Int = 0

    @ColumnInfo(name = Constants.DB.AD_CLICKED_NUMBER)
    var adClickedNumber: Int = 0
}