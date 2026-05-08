package com.openstream.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openstream.app.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "settings")

@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val APP_THEME = stringPreferencesKey("app_theme")
    }

    val themeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[APP_THEME] ?: ThemeMode.DARK.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.DARK)
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { it[APP_THEME] = mode.name }
    }
}
