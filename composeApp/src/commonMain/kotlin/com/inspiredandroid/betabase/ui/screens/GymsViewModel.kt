package com.inspiredandroid.betabase.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.FilterStorage
import com.inspiredandroid.betabase.data.Gym
import com.inspiredandroid.betabase.data.GymsFilters
import com.inspiredandroid.betabase.data.GymsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class GymsUiState(
    val gyms: List<Gym> = emptyList(),
    val filters: GymsFilters = GymsFilters.Default,
) {
    val filteredGyms: List<Gym> by lazy { gyms.filter(filters::matches) }
}

class GymsViewModel(
    private val repository: GymsRepository = GymsRepository(),
    private val filterStorage: FilterStorage? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(
        GymsUiState(filters = filterStorage?.loadGyms() ?: GymsFilters.Default),
    )
    val state: StateFlow<GymsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val loaded = repository.loadAll()
            _state.update { it.copy(gyms = loaded) }
        }
    }

    fun toggle(discipline: Discipline) = updateFilters { it.toggle(discipline) }

    fun toggleBoards() = updateFilters { it.toggleBoards() }

    private inline fun updateFilters(transform: (GymsFilters) -> GymsFilters) {
        _state.update { it.copy(filters = transform(it.filters)) }
        filterStorage?.saveGyms(_state.value.filters)
    }
}
