package com.openstream.library

object StreamLibrary {
    const val VERSION = "1.0.0"
    private var initialized = false
    fun init() { if (!initialized) initialized = true }
    fun isInitialized(): Boolean = initialized
}
