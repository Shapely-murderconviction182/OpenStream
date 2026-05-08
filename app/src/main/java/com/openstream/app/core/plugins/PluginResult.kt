package com.openstream.app.core.plugins

sealed class PluginResult {
    data class Success(val instance: Any) : PluginResult()
    data class Failure(val error: String) : PluginResult()
}
