package dev.zhafran.velnyx.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ActiveRunEntity::class,
        ActiveRunPointEntity::class,
        ActiveRunSegmentEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activeRunDao(): ActiveRunDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "velnyx_db",
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
    }
}

