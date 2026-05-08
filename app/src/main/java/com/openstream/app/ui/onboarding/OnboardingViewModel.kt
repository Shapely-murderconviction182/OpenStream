package com.openstream.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openstream.app.data.OnboardingRepository
import com.openstream.app.data.repository.ExtensionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class OnboardingState(
    val currentPage        : Int             = 0,
    val selectedPrefs      : Set<String>     = emptySet(),
    val extensionLoadMsg   : String          = "Setting up your sources...",
    val extensionLoadDone  : Boolean         = false,
    val extensionCount     : Int             = 0
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepo  : OnboardingRepository,
    private val extensionRepo   : ExtensionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun nextPage() { _state.value = _state.value.copy(currentPage = _state.value.currentPage + 1) }
    fun prevPage() { if (_state.value.currentPage > 0) _state.value = _state.value.copy(currentPage = _state.value.currentPage - 1) }

    fun togglePreference(pref: String) {
        val cur = _state.value.selectedPrefs.toMutableSet()
        if (!cur.add(pref)) cur.remove(pref)
        _state.value = _state.value.copy(selectedPrefs = cur)
    }

    fun loadExtensions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(extensionLoadMsg = "Setting up your sources...", extensionLoadDone = false)
            withTimeoutOrNull(10_000L) {
                runCatching { extensionRepo.refreshFromNetwork() }
            }
            _state.value = _state.value.copy(
                extensionLoadDone = true,
                extensionLoadMsg  = "Sources ready!"
            )
        }
    }

    fun saveAndFinish(onDone: () -> Unit) {
        viewModelScope.launch {
            onboardingRepo.savePreferences(_state.value.selectedPrefs)
            onboardingRepo.setOnboardingComplete()
            onDone()
        }
    }
}
