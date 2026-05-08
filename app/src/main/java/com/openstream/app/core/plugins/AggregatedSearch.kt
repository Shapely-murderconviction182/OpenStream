package com.openstream.app.core.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AggregatedSearch @Inject constructor(
    private val extensionManager: ExtensionManager
) {
    /**
     * Searches ALL loaded plugins in parallel.
     * Emits incremental results as each plugin responds.
     * Deduplicates by URL.
     * Never throws â€” per-plugin errors are swallowed.
     */
    fun searchAll(query: String): Flow<List<SearchResponse>> = flow {
        val plugins = extensionManager.getLoadedPlugins()
        if (plugins.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val seen       = mutableSetOf<String>()
        val cumulative = mutableListOf<SearchResponse>()

        coroutineScope {
            plugins.values.map { bridge ->
                async(Dispatchers.IO) {
                    runCatching { bridge.search(query) }.getOrDefault(emptyList())
                }
            }.forEach { deferred ->
                val results = deferred.await()
                val newItems = results.filter { seen.add(it.url) }
                if (newItems.isNotEmpty()) {
                    cumulative.addAll(newItems)
                    emit(cumulative.toList())
                }
            }
        }

        if (cumulative.isEmpty()) emit(emptyList())
    }.flowOn(Dispatchers.IO)
}
