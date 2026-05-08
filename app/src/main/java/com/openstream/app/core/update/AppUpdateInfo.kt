package com.openstream.app.core.update

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateInfo(
    val versionCode : Int    = 0,
    val versionName : String = "",
    val apkUrl      : String = "",
    val changelog   : String = ""
)
