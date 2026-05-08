package com.openstream.app.data.repository

import com.openstream.app.data.local.ExtensionDao
import com.openstream.app.data.local.ExtensionEntity
import com.openstream.app.data.remote.ExtensionApiService
import com.openstream.app.data.remote.RepoConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionRepository @Inject constructor(
    private val api : ExtensionApiService,
    private val dao : ExtensionDao
) {
    /** Live Room stream â€” emits on every DB change. */
    fun observeExtensions(): Flow<List<ExtensionEntity>> = dao.getAll()

    /**
     * Fetch all repos in parallel.
     * Failed repos are silently skipped.
     * If ALL fail â†’ DB retains existing cached data (no wipe).
     */
    suspend fun refreshFromNetwork() {
        val results: List<ExtensionEntity> = coroutineScope {
            RepoConfig.REPO_URLS.map { url ->
                async(Dispatchers.IO) {
                    runCatching {
                        api.fetchRepo(url).map { dto ->
                            dto.toEntity(RepoConfig.repoNameFrom(url))
                        }
                    }.getOrDefault(emptyList())
                }
            }.awaitAll().flatten()
        }
        if (results.isNotEmpty()) {
            dao.clearAndInsert(results)
        }
        // results empty â†’ keep stale cache, caller decides how to handle
    }
}
