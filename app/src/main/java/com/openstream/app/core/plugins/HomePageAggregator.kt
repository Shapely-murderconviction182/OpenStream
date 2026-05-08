package com.openstream.app.core.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

sealed class HomeAggState {
    object Loading                                 : HomeAggState()
    data class Partial(val sections: List<HomeSection>) : HomeAggState()
    data class Complete(val sections: List<HomeSection>): HomeAggState()
    data class Error(val message: String)          : HomeAggState()
}

@Singleton
class HomePageAggregator @Inject constructor(
    private val extensionManager: ExtensionManager
) {
    /**
     * Calls getMainPage(1) on all loaded plugins in parallel.
     * Emits: Loading â†’ Partial (incremental) â†’ Complete.
     */
    fun getHomePage(): Flow<HomeAggState> = flow {
        emit(HomeAggState.Loading)

        val plugins = extensionManager.getLoadedPlugins()
        if (plugins.isEmpty()) {
            emit(HomeAggState.Complete(emptyList()))
            return@flow
        }

        val allSections = mutableListOf<HomeSection>()

        coroutineScope {
            plugins.values.map { bridge ->
                async(Dispatchers.IO) {
                    runCatching { bridge.getMainPage(1) }.getOrDefault(emptyList())
                }
            }.forEach { deferred ->
                val sections = deferred.await()
                if (sections.isNotEmpty()) {
                    allSections.addAll(sections)
                    emit(HomeAggState.Partial(allSections.toList()))
                }
            }
        }

        emit(HomeAggState.Complete(allSections.toList()))
    }.flowOn(Dispatchers.IO)
}
