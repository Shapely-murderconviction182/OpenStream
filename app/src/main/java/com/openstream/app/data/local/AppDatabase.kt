package com.openstream.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities     = [ExtensionEntity::class],
    version      = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun extensionDao(): ExtensionDao
}
