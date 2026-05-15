package com.inspiredandroid.betabase.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspiredandroid.betabase.data.CompetitionEvent
import com.inspiredandroid.betabase.data.CompetitionsFilters
import com.inspiredandroid.betabase.data.CompetitionsRepository
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.FilterStorage
import com.inspiredandroid.betabase.data.Gender
import com.inspiredandroid.betabase.data.IfscVideo
import com.inspiredandroid.betabase.data.Round
import com.inspiredandroid.betabase.data.SourceTag
import com.inspiredandroid.betabase.data.VideosRepository
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
    val streams: List<IfscVideo> = emptyList(),
    val filters: CompetitionsFilters = CompetitionsFilters.Default,
) {
    val filteredEvents: List<CompetitionEvent> by lazy { events.filter(filters::matches) }
    val filteredStreams: List<IfscVideo> by lazy { streams.filter(filters::matches) }

    val showInitialLoading: Boolean get() = initialLoading && events.isEmpty()
    val showError: Boolean get() = errorMessage != null && events.isEmpty()
}

class CompetitionsViewModel(
    private val repository: CompetitionsRepository,
    private val videosRepository: VideosRepository? = null,
    private val filterStorage: FilterStorage? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CompetitionsUiState(filters = filterStorage?.load() ?: CompetitionsFilters.Default),
    )
    val state: StateFlow<CompetitionsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    fun toggle(source: SourceTag) = updateFilters { it.toggle(source) }

    fun toggle(discipline: Discipline) = updateFilters { it.toggle(discipline) }

    fun toggle(round: Round) = updateFilters { it.toggle(round) }

    fun toggle(gender: Gender) = updateFilters { it.toggle(gender) }

    fun toggleIncludePara() = updateFilters { it.toggleIncludePara() }

    private inline fun updateFilters(transform: (CompetitionsFilters) -> CompetitionsFilters) {
        _state.update { it.copy(filters = transform(it.filters)) }
        filterStorage?.save(_state.value.filters)
    }

    private fun load() {
        viewModelScope.launch {
            val isFirstLoad = _state.value.events.isEmpty()
            _state.update {
                it.copy(refreshing = !isFirstLoad, errorMessage = null)
            }
            repository.loadUpcoming().collect { progress ->
                _state.update { current ->
                    val allFailed = progress.done && progress.events.isEmpty() && progress.errors.isNotEmpty()
                    current.copy(
                        // First load: show events as soon as any source returns so bundled
                        // data lights up the screen without waiting for slow network feeds.
                        // Refresh: keep old events visible until everything is done to avoid flicker.
                        events = if (isFirstLoad || progress.done) progress.events else current.events,
                        initialLoading = isFirstLoad && !progress.done && progress.events.isEmpty(),
                        refreshing = !progress.done,
                        errorMessage = if (allFailed) {
                            progress.errors.first().message?.takeIf { msg -> msg.isNotBlank() }
                                ?: "Could not load competitions."
                        } else {
                            null
                        },
                    )
                }
            }
        }
        videosRepository?.let { repo ->
            viewModelScope.launch {
                repo.loadRecent()
                    .onSuccess { videos -> _state.update { it.copy(streams = videos) } }
            }
        }
    }
}
