package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChannelEntity::class,
        CategoryEntity::class,
        MatchEntity::class,
        MediaEntity::class,
        FavoriteEntity::class,
        ApiSourceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SaribDatabase : RoomDatabase() {
    abstract fun saribDao(): SaribDao

    companion object {
        @Volatile
        private var INSTANCE: SaribDatabase? = null

        fun getDatabase(context: Context): SaribDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SaribDatabase::class.java,
                    "sarib_tv_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
