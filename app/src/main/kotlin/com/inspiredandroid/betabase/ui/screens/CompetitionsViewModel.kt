package com.inspiredandroid.betabase.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inspiredandroid.betabase.data.BundledJsonEventSource
import com.inspiredandroid.betabase.data.CompetitionEvent
import com.inspiredandroid.betabase.data.CompetitionsFilters
import com.inspiredandroid.betabase.data.CompetitionsRepository
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.Gender
import com.inspiredandroid.betabase.data.IfscEventSource
import com.inspiredandroid.betabase.data.Round
import com.inspiredandroid.betabase.data.SourceTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompetitionsUiState(
    val initialLoading: Boolean = true,
    val refreshing: Boolean = false,
    val errorMessage: String? = null,
    val events: List<CompetitionEvent> = emptyList(),
    val filters: CompetitionsFilters = CompetitionsFilters.Default,
) {
    val filteredEvents: List<CompetitionEvent>
        get() = events.filter(filters::matches)

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

    fun toggle(source: SourceTag) =
        _state.update { it.copy(filters = it.filters.toggle(source)) }

    fun toggle(discipline: Discipline) =
        _state.update { it.copy(filters = it.filters.toggle(discipline)) }

    fun toggle(round: Round) =
        _state.update { it.copy(filters = it.filters.toggle(round)) }

    fun toggle(gender: Gender) =
        _state.update { it.copy(filters = it.filters.toggle(gender)) }

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val repo = CompetitionsRepository(
                    sources = listOf(
                        IfscEventSource(),
                        BundledJsonEventSource(
                            context = app.applicationContext,
                            assetName = "nkbv_competitions.json",
                            tag = SourceTag.NKBV,
                        ),
                        BundledJsonEventSource(
                            context = app.applicationContext,
                            assetName = "sca_competitions.json",
                            tag = SourceTag.SCA,
                        ),
                    ),
                )
                CompetitionsViewModel(repo)
            }
        }
    }
}
