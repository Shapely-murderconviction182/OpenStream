package com.openstream.app.core.plugins

sealed class UpdateResult {
    data class Success(val count: Int)                   : UpdateResult()
    object NoUpdates                                     : UpdateResult()
    object Offline                                       : UpdateResult()
    data class PartialFailure(val failed: List<String>)  : UpdateResult()
}
