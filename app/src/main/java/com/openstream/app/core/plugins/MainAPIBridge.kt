package com.openstream.app.core.plugins

import android.util.Log

/**
 * Reflection wrapper for plugin interaction.
 * Zero compile-time dependency on plugin classes.
 */
class MainAPIBridge(private val instance: Any) {

    private val tag   = "MainAPIBridge"
    private val clazz = instance::class.java

    // â”€â”€ Identity â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun getName(): String    = callStringMethod("getName")    ?: callField("name")    ?: "Unknown"
    fun getMainUrl(): String = callStringMethod("getMainUrl") ?: callField("mainUrl") ?: ""

    // â”€â”€ Plugin operations â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun search(query: String): List<SearchResponse> {
        return try {
            val method = clazz.getMethod("search", String::class.java)
            @Suppress("UNCHECKED_CAST")
            val raw = method.invoke(instance, query) as? List<*> ?: emptyList<Any>()
            raw.mapNotNull { mapToSearchResponse(it) }
        } catch (e: Exception) {
            Log.w(tag, "search() failed on ${getName()}: ${e.message}")
            emptyList()
        }
    }

    fun load(url: String): LoadResponse? {
        return try {
            val method = clazz.getMethod("load", String::class.java)
            val raw    = method.invoke(instance, url)
            mapToLoadResponse(raw)
        } catch (e: Exception) {
            Log.w(tag, "load() failed on ${getName()}: ${e.message}")
            null
        }
    }

    fun getMainPage(page: Int): List<HomeSection> {
        return try {
            val method = clazz.getMethod("getMainPage", Int::class.java)
            @Suppress("UNCHECKED_CAST")
            val raw = method.invoke(instance, page) as? List<*> ?: emptyList<Any>()
            raw.mapNotNull { mapToHomeSection(it) }
        } catch (e: Exception) {
            Log.w(tag, "getMainPage() failed on ${getName()}: ${e.message}")
            emptyList()
        }
    }

    // â”€â”€ Mapping helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private fun mapToSearchResponse(obj: Any?): SearchResponse? {
        obj ?: return null
        return runCatching {
            val c = obj::class.java
            SearchResponse(
                name      = c.readString(obj, "name"),
                url       = c.readString(obj, "url"),
                posterUrl = c.readString(obj, "posterUrl"),
                type      = c.readString(obj, "type"),
                sourceName = getName()
            )
        }.getOrNull()
    }

    private fun mapToLoadResponse(obj: Any?): LoadResponse? {
        obj ?: return null
        return runCatching {
            val c = obj::class.java
            LoadResponse(
                name        = c.readString(obj, "name"),
                url         = c.readString(obj, "url"),
                description = c.readString(obj, "plot"),
                posterUrl   = c.readString(obj, "posterUrl"),
                sourceName  = getName()
            )
        }.getOrNull()
    }

    private fun mapToHomeSection(obj: Any?): HomeSection? {
        obj ?: return null
        return runCatching {
            val c     = obj::class.java
            val title = c.readString(obj, "name")
            @Suppress("UNCHECKED_CAST")
            val list  = c.readField<List<*>>(obj, "list") ?: emptyList<Any>()
            HomeSection(
                title  = title,
                items  = list.mapNotNull { mapToSearchResponse(it) },
                source = getName()
            )
        }.getOrNull()
    }

    // â”€â”€ Reflection utilities â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private fun callStringMethod(name: String): String? = runCatching {
        clazz.getMethod(name).invoke(instance) as? String
    }.getOrNull()

    private fun callField(name: String): String? = runCatching {
        clazz.getField(name).get(instance) as? String
    }.getOrNull()

    private fun Class<*>.readString(obj: Any, fieldName: String): String =
        readField<String>(obj, fieldName) ?: ""

    @Suppress("UNCHECKED_CAST")
    private fun <T> Class<*>.readField(obj: Any, fieldName: String): T? = runCatching {
        val f = try { getField(fieldName) } catch (_: Exception) { getDeclaredField(fieldName).also { it.isAccessible = true } }
        f.get(obj) as? T
    }.getOrNull()
}

// â”€â”€ Internal models â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

data class SearchResponse(
    val name       : String,
    val url        : String,
    val posterUrl  : String = "",
    val type       : String = "",
    val sourceName : String = ""
)

data class LoadResponse(
    val name        : String,
    val url         : String,
    val description : String = "",
    val posterUrl   : String = "",
    val sourceName  : String = ""
)

data class HomeSection(
    val title  : String,
    val items  : List<SearchResponse>,
    val source : String = ""
)
