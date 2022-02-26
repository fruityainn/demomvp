package com.hide.videophoto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.MyApplication
import com.hide.videophoto.data.dao.ActivityDao
import com.hide.videophoto.data.dao.AuthDao
import com.hide.videophoto.data.dao.FileDao
import com.hide.videophoto.data.entity.ActivityEntity
import com.hide.videophoto.data.entity.AuthEntity
import com.hide.videophoto.data.entity.FileEntity

@Database(
    entities = [FileEntity::class, AuthEntity::class, ActivityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fileDao(): FileDao
    abstract fun authDao(): AuthDao
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(ctx: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext,
                    AppDatabase::class.java,
                    Constants.DB_PATH
                )
                    .addCallback(dbCallback)
                    .enableMultiInstanceInvalidation()
                    .build()
                INSTANCE = instance

                instance
            }
        }

        private val dbCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val defaultFolder = MyApplication.instance.getString(R.string.folder_default)

                val currentTime = System.currentTimeMillis() / 1000
                val query = "INSERT INTO ${Constants.DB.FILE_ENTITY}(" +
                        "${Constants.DB.NAME}, ${Constants.DB.DIRECTORY}, ${Constants.DB.SIZE}, ${Constants.DB.DURATION}," +
                        "${Constants.DB.FAVORITE}, ${Constants.DB.NOTE}, ${Constants.DB.ADDED_DATE}, ${Constants.DB.MODIFIED_DATE}" +
                        ")" +
                        "VALUES" +
                        "(\"$defaultFolder\", 1, 0, 0, 0, 0, $currentTime, $currentTime)"
                db.execSQL(query)
            }
        }
    }
}