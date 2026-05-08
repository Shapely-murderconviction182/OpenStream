package com.openstream.app.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.plugins.AggregatedSearch
import com.openstream.app.core.plugins.SearchResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchUiState {
    object Idle                                           : SearchUiState()
    object Loading                                        : SearchUiState()
    object Empty                                          : SearchUiState()
    data class Success(val results: List<SearchResponse>) : SearchUiState()
    data class Error(val message: String)                 : SearchUiState()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val aggregatedSearch: AggregatedSearch
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        if (query.isBlank()) { _uiState.value = SearchUiState.Idle; return }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                aggregatedSearch.searchAll(query).collect { results ->
                    _uiState.value = if (results.isEmpty()) SearchUiState.Empty
                                     else SearchUiState.Success(results)
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun clear() { searchJob?.cancel(); _uiState.value = SearchUiState.Idle }
}
