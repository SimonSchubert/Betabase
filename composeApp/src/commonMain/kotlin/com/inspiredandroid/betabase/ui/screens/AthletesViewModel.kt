package com.inspiredandroid.betabase.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspiredandroid.betabase.data.Athlete
import com.inspiredandroid.betabase.data.AthleteGender
import com.inspiredandroid.betabase.data.AthleteVideosRepository
import com.inspiredandroid.betabase.data.AthletesRepository
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.YoutubeVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AthletesFilters(
    val genders: Set<AthleteGender> = AthleteGender.entries.toSet(),
    val disciplines: Set<Discipline> = emptySet(),
    val query: String = "",
) {
    fun toggle(gender: AthleteGender): AthletesFilters = copy(genders = if (gender in genders) genders - gender else genders + gender)

    fun toggle(discipline: Discipline): AthletesFilters = copy(disciplines = if (discipline in disciplines) disciplines - discipline else disciplines + discipline)

    fun matches(athlete: Athlete): Boolean {
        if (athlete.gender !in genders) return false
        if (disciplines.isNotEmpty() && disciplines.none { athlete.titlesIn(it) > 0 }) return false
        val q = query.trim()
        if (q.isNotEmpty()) {
            val needle = q.lowercase()
            val haystack = buildString {
                append(athlete.firstName).append(' ').append(athlete.lastName)
                athlete.country?.let { append(' ').append(it) }
            }.lowercase()
            if (needle !in haystack) return false
        }
        return true
    }
}

fun Athlete.titlesIn(discipline: Discipline): Int = when (discipline) {
    Discipline.LEAD -> leadTitles
    Discipline.BOULDER -> boulderTitles
    Discipline.SPEED -> speedTitles
    Discipline.COMBINED -> combinedTitles
    Discipline.OTHER -> 0
}

@Immutable
data class AthletesUiState(
    val athletes: List<Athlete> = emptyList(),
    val filters: AthletesFilters = AthletesFilters(),
    val loading: Boolean = true,
    val videosByChannel: Map<String, List<YoutubeVideo>> = emptyMap(),
) {
    val filtered: List<Athlete> by lazy { athletes.filter(filters::matches) }
}

class AthletesViewModel(
    private val repository: AthletesRepository = AthletesRepository(),
    private val videosRepository: AthleteVideosRepository? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(AthletesUiState())
    val state: StateFlow<AthletesUiState> = _state.asStateFlow()

    // Channels we've already kicked off a fetch for, so cards scrolling back
    // into view (or duplicate compositions) don't refire the request.
    private val requestedChannels = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            val loaded = repository.load()
                .sortedWith(
                    compareByDescending<Athlete> { it.totalGolds }
                        .thenByDescending { it.totalTitles }
                        .thenBy { it.lastName },
                )
            _state.update { it.copy(athletes = loaded, loading = false) }
        }
    }

    /** Loads the channel's latest videos once; no-op without a videos repository. */
    fun ensureVideos(channelId: String) {
        val repo = videosRepository ?: return
        if (!requestedChannels.add(channelId)) return
        viewModelScope.launch {
            repo.load(channelId)
                .onSuccess { videos ->
                    if (videos.isNotEmpty()) {
                        _state.update { it.copy(videosByChannel = it.videosByChannel + (channelId to videos)) }
                    }
                }
                .onFailure { requestedChannels.remove(channelId) } // allow a later retry
        }
    }

    fun toggle(gender: AthleteGender) = _state.update { it.copy(filters = it.filters.toggle(gender)) }
    fun toggle(discipline: Discipline) = _state.update { it.copy(filters = it.filters.toggle(discipline)) }
    fun setQuery(query: String) = _state.update { it.copy(filters = it.filters.copy(query = query)) }
}
