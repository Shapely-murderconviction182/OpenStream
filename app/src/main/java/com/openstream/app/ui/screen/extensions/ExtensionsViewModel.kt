package com.openstream.app.ui.screen.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openstream.app.data.local.ExtensionEntity
import com.openstream.app.data.repository.ExtensionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExtensionsUiState {
    object Loading                                          : ExtensionsUiState()
    data class Success(val extensions: List<ExtensionEntity>,
                       val updatedCount: Int = 0)           : ExtensionsUiState()
    data class Error(val message: String)                   : ExtensionsUiState()
}

@HiltViewModel
class ExtensionsViewModel @Inject constructor(
    private val repository: ExtensionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExtensionsUiState>(ExtensionsUiState.Loading)
    val uiState: StateFlow<ExtensionsUiState> = _uiState.asStateFlow()

    @Volatile private var hasData      = false
    @Volatile private var updatedCount = 0

    init {
        viewModelScope.launch {
            repository.observeExtensions().collect { list ->
                if (list.isNotEmpty()) {
                    hasData = true
                    _uiState.value = ExtensionsUiState.Success(list, updatedCount)
                } else if (!hasData) {
                    _uiState.value = ExtensionsUiState.Loading
                }
            }
        }
        refresh()
    }

    fun refresh() {
        if (!hasData) _uiState.value = ExtensionsUiState.Loading
        viewModelScope.launch {
            runCatching { repository.refreshFromNetwork() }
                .onFailure {
                    if (_uiState.value is ExtensionsUiState.Loading)
                        _uiState.value = ExtensionsUiState.Error(it.message ?: "Fetch failed")
                }
        }
    }

    fun onUpdatesApplied(count: Int) {
        updatedCount = count
        val cur = _uiState.value
        if (cur is ExtensionsUiState.Success)
            _uiState.value = cur.copy(updatedCount = count)
    }
}
