package com.openstream.app.data.remote

import com.openstream.app.data.local.ExtensionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExtensionDto(
    val id          : String = "",
    val name        : String = "",
    @SerialName("icon_url")   val iconUrl     : String = "",
    val description : String = "",
    val version     : String = "1.0.0",
    val language    : String = "en",
    val type        : String = "unknown",
    @SerialName("apk_url")    val apkUrl      : String = ""
) {
    fun toEntity(repoName: String): ExtensionEntity = ExtensionEntity(
        id          = id.ifBlank { "${repoName}_${name.replace(" ", "_").lowercase()}" },
        name        = name,
        repoName    = repoName,
        iconUrl     = iconUrl,
        description = description,
        version     = version,
        language    = language,
        type        = type,
        apkUrl      = apkUrl
    )
}
