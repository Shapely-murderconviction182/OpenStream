package com.openstream.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExtensionDao {

    @Query("SELECT * FROM extensions ORDER BY language ASC, name ASC")
    abstract fun getAll(): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM extensions WHERE isInstalled = 1")
    abstract suspend fun getAllInstalled(): List<ExtensionEntity>

    @Query("SELECT * FROM extensions WHERE id = :id OR name = :id LIMIT 1")
    abstract suspend fun findById(id: String): ExtensionEntity?

    @Query("SELECT * FROM extensions WHERE updateAvailable = 1 AND isInstalled = 1")
    abstract suspend fun getPendingUpdates(): List<ExtensionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(extensions: List<ExtensionEntity>)

    @Update
    abstract suspend fun update(extension: ExtensionEntity)

    @Query("DELETE FROM extensions")
    abstract suspend fun clearAll()

    @Transaction
    open suspend fun clearAndInsert(extensions: List<ExtensionEntity>) {
        clearAll()
        insertAll(extensions)
    }
}
