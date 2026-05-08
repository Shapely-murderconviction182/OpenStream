package com.openstream.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extensions")
data class ExtensionEntity(
    @PrimaryKey val id          : String,
    val name           : String,
    val repoName       : String,
    val iconUrl        : String,
    val description    : String,
    val version        : String  = "1.0.0",
    val language       : String,
    val type           : String,
    val apkUrl         : String,
    val isInstalled    : Boolean = false,
    val localPath      : String? = null,
    val lastUpdated    : Long    = 0L,
    val updateAvailable: Boolean = false,
    val status         : ExtensionStatus = ExtensionStatus.NOT_INSTALLED
)
