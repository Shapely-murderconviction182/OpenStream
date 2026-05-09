package com.openstream.app.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.plugins.ExtensionManager
import com.openstream.app.core.plugins.LoadResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    object Loading                             : DetailUiState()
    data class Success(val data: LoadResponse) : DetailUiState()
    data class Error(val message: String)      : DetailUiState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val extensionManager: ExtensionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(url: String) {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val plugins = extensionManager.getLoadedPlugins()
            if (plugins.isEmpty()) {
                _uiState.value = DetailUiState.Success(
                    LoadResponse(name = url, url = url, description = "No plugins loaded.")
                )
                return@launch
            }
            // Try each plugin until one returns a result
            var result: LoadResponse? = null
            for (bridge in plugins.values) {
                result = runCatching { bridge.load(url) }.getOrNull()
                if (result != null) break
            }
            _uiState.value = if (result != null) {
                DetailUiState.Success(result)
            } else {
                DetailUiState.Error("Could not load details for this item.")
            }
        }
    }
}
