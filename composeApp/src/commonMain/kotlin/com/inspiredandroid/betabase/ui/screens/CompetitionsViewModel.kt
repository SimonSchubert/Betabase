package com.inspiredandroid.betabase.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspiredandroid.betabase.data.CompetitionEvent
import com.inspiredandroid.betabase.data.CompetitionsFilters
import com.inspiredandroid.betabase.data.CompetitionsRepository
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.Gender
import com.inspiredandroid.betabase.data.Round
import com.inspiredandroid.betabase.data.SourceTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class CompetitionsUiState(
    val initialLoading: Boolean = true,
    val refreshing: Boolean = false,
    val errorMessage: String? = null,
    val events: List<CompetitionEvent> = emptyList(),
    val filters: CompetitionsFilters = CompetitionsFilters.Default,
) {
    val filteredEvents: List<CompetitionEvent> by lazy { events.filter(filters::matches) }

    val showInitialLoading: Boolean get() = initialLoading && events.isEmpty()
    val showError: Boolean get() = errorMessage != null && events.isEmpty()
}

class CompetitionsViewModel(
    private val repository: CompetitionsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CompetitionsUiState())
    val state: StateFlow<CompetitionsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    fun toggle(source: SourceTag) = _state.update { it.copy(filters = it.filters.toggle(source)) }

    fun toggle(discipline: Discipline) = _state.update { it.copy(filters = it.filters.toggle(discipline)) }

    fun toggle(round: Round) = _state.update { it.copy(filters = it.filters.toggle(round)) }

    fun toggle(gender: Gender) = _state.update { it.copy(filters = it.filters.toggle(gender)) }

    private fun load() {
        viewModelScope.launch {
            _state.update {
                it.copy(refreshing = it.events.isNotEmpty(), errorMessage = null)
            }
            runCatching { repository.loadUpcoming() }
                .onSuccess { events ->
                    _state.update {
                        it.copy(
                            initialLoading = false,
                            refreshing = false,
                            errorMessage = null,
                            events = events,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            initialLoading = false,
                            refreshing = false,
                            errorMessage = e.message?.takeIf { msg -> msg.isNotBlank() }
                                ?: "Could not load competitions.",
                        )
                    }
                }
        }
    }
}
