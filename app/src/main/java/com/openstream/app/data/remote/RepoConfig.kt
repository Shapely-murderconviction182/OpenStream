package com.openstream.app.data.remote

/**
 * Add your extension repository index URLs here.
 * Each URL must return a JSON array matching ExtensionDto.
 *
 * Expected JSON format per item:
 * {
 *   "id":          "com.example.ext",
 *   "name":        "My Extension",
 *   "icon_url":    "https://...",
 *   "description": "Some description",
 *   "version":     "1.0.0",
 *   "language":    "en",
 *   "type":        "anime",
 *   "apk_url":     "https://..."
 * }
 */
object RepoConfig {

    val REPO_URLS: List<String> = listOf(
        "https://raw.githubusercontent.com/Kevin-JT/OpenStream-Extensions/builds/repo.json"
        // Add more repo URLs here
    )

    fun repoNameFrom(url: String): String = runCatching {
        url.substringAfterLast("/")
            .substringBefore(".json")
            .replace("-", " ")
            .replaceFirstChar { it.uppercase() }
    }.getOrDefault("Unknown Repo")
}
