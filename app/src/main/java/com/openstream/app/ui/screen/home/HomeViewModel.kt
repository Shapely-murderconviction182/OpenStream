package com.openstream.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.plugins.HomeAggState
import com.openstream.app.core.plugins.HomePageAggregator
import com.openstream.app.core.plugins.HomeSection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading                                  : HomeUiState()
    object Empty                                    : HomeUiState()
    data class Success(val sections: List<HomeSection>) : HomeUiState()
    data class Error(val message: String)           : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val aggregator: HomePageAggregator
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            aggregator.getHomePage().collect { state ->
                _uiState.value = when (state) {
                    is HomeAggState.Loading  -> HomeUiState.Loading
                    is HomeAggState.Partial  -> HomeUiState.Success(state.sections)
                    is HomeAggState.Complete -> if (state.sections.isEmpty()) HomeUiState.Empty
                                               else HomeUiState.Success(state.sections)
                    is HomeAggState.Error    -> HomeUiState.Error(state.message)
                }
            }
        }
    }
}
