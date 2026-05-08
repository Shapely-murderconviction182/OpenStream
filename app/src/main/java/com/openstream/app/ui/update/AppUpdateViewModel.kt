package com.openstream.app.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.update.AppUpdateInfo
import com.openstream.app.core.update.AppUpdateManager
import com.openstream.app.core.update.AppUpdateResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppUpdateUiState {
    object Checking                                  : AppUpdateUiState()
    object NoUpdate                                  : AppUpdateUiState()
    data class UpdateAvailable(val info: AppUpdateInfo): AppUpdateUiState()
    data class Error(val message: String)            : AppUpdateUiState()
}

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val updateManager: AppUpdateManager
) : ViewModel() {

    private val _state = MutableStateFlow<AppUpdateUiState>(AppUpdateUiState.Checking)
    val state: StateFlow<AppUpdateUiState> = _state.asStateFlow()

    init { checkForUpdate() }

    fun checkForUpdate() {
        viewModelScope.launch {
            _state.value = AppUpdateUiState.Checking
            _state.value = when (val result = updateManager.checkForUpdate()) {
                is AppUpdateResult.UpdateAvailable -> AppUpdateUiState.UpdateAvailable(result.info)
                is AppUpdateResult.NoUpdate        -> AppUpdateUiState.NoUpdate
                is AppUpdateResult.Error           -> AppUpdateUiState.Error(result.message)
            }
        }
    }

    fun dismiss() { _state.value = AppUpdateUiState.NoUpdate }
}
