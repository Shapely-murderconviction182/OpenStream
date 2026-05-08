package com.openstream.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingStore: DataStore<Preferences>
    by preferencesDataStore(name = "onboarding")

@Singleton
class OnboardingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_COMPLETE    = booleanPreferencesKey("onboarding_complete")
        val KEY_PREFERENCES = stringSetPreferencesKey("content_preferences")
    }

    val isOnboardingComplete: Flow<Boolean> = context.onboardingStore.data
        .map { it[KEY_COMPLETE] ?: false }

    suspend fun setOnboardingComplete() {
        context.onboardingStore.edit { it[KEY_COMPLETE] = true }
    }

    suspend fun savePreferences(prefs: Set<String>) {
        context.onboardingStore.edit { it[KEY_PREFERENCES] = prefs }
    }
}
