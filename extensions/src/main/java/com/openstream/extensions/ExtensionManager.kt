package com.openstream.extensions

import com.openstream.library.StreamLibrary

data class Extension(val id: String, val name: String, val version: String, val description: String = "")

object ExtensionManager {
    private val registry = mutableMapOf<String, Extension>()
    fun register(e: Extension) {
        check(StreamLibrary.isInitialized()) { "StreamLibrary not initialized." }
        registry[e.id] = e
    }
    fun unregister(id: String)    { registry.remove(id) }
    fun getAll(): List<Extension> = registry.values.toList()
    fun findById(id: String)      = registry[id]
    fun clear()                   { registry.clear() }
}
