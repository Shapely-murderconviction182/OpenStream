package com.openstream.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openstream.app.data.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(private val repo: ThemeRepository) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repo.themeFlow.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ThemeMode.DARK
    )

    fun setTheme(mode: ThemeMode) { viewModelScope.launch { repo.setTheme(mode) } }
}
