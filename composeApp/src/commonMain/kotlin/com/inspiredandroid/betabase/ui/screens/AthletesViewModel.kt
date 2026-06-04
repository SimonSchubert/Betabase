package com.inspiredandroid.betabase.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspiredandroid.betabase.data.Athlete
import com.inspiredandroid.betabase.data.AthleteGender
import com.inspiredandroid.betabase.data.AthleteVideosRepository
import com.inspiredandroid.betabase.data.AthletesRepository
import com.inspiredandroid.betabase.data.YoutubeVideo
import com.inspiredandroid.betabase.data.isActive
import com.inspiredandroid.betabase.data.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AthletesFilters(
    val genders: Set<AthleteGender> = AthleteGender.entries.toSet(),
    val country: String? = null,
    val query: String = "",
    val showInactive: Boolean = false,
) {
    fun toggle(gender: AthleteGender): AthletesFilters = copy(genders = if (gender in genders) genders - gender else genders + gender)

    /** Selects a country, or clears the filter when the already-selected one is tapped again. */
    fun withCountry(country: String?): AthletesFilters = copy(country = if (country == this.country) null else country)

    fun matches(athlete: Athlete): Boolean {
        if (athlete.gender !in genders) return false
        if (country != null && athlete.country != country) return false
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

/** One selectable country chip: canonical name, flag code, and how many athletes carry it. */
@Immutable
data class CountryFacet(val name: String, val code: String?, val count: Int)

@Immutable
data class AthletesUiState(
    val athletes: List<Athlete> = emptyList(),
    val filters: AthletesFilters = AthletesFilters(),
    val loading: Boolean = true,
    val videosByChannel: Map<String, List<YoutubeVideo>> = emptyMap(),
    val currentYear: Int = 0,
) {
    val filtered: List<Athlete> by lazy {
        athletes.filter { filters.matches(it) && (filters.showInactive || it.isActive(currentYear)) }
    }

    /** Countries present in the data, most-represented first, for the filter row. */
    val countries: List<CountryFacet> by lazy {
        athletes.mapNotNull { a -> a.country?.let { it to a.countryCode } }
            .groupBy({ it.first }, { it.second })
            .map { (name, codes) -> CountryFacet(name, codes.firstOrNull { it != null }, codes.size) }
            .sortedWith(compareByDescending<CountryFacet> { it.count }.thenBy { it.name })
    }
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

    // The retired roster lives in a separate bundled file loaded only once the
    // user asks to see inactive athletes; this guards against re-loading it.
    private var retiredRequested = false

    init {
        viewModelScope.launch {
            val loaded = repository.load().sortedByRank()
            _state.update { it.copy(athletes = loaded, loading = false, currentYear = today().year) }
        }
    }

    private fun List<Athlete>.sortedByRank(): List<Athlete> = sortedWith(
        compareByDescending<Athlete> { it.totalGolds }
            .thenByDescending { it.totalTitles }
            .thenBy { it.lastName },
    )

    /** Loads and merges the long-retired roster the first time it's needed. */
    private fun ensureRetiredLoaded() {
        if (retiredRequested) return
        retiredRequested = true
        viewModelScope.launch {
            val retired = repository.loadRetired()
            if (retired.isEmpty()) return@launch
            _state.update { state ->
                val ids = state.athletes.mapTo(HashSet()) { it.id }
                val merged = (state.athletes + retired.filter { it.id !in ids }).sortedByRank()
                state.copy(athletes = merged)
            }
        }
    }

    /** Loads the channel's latest videos once; no-op without a videos repository. */
    fun ensureVideos(channelId: String) {
        val repo = videosRepository ?: return
        if (!requestedChannels.add(channelId)) return
        viewModelScope.launch {
            repo.load(channelId).collect { result ->
                result
                    .onSuccess { videos ->
                        if (videos.isNotEmpty()) {
                            _state.update { it.copy(videosByChannel = it.videosByChannel + (channelId to videos)) }
                        }
                    }
                    .onFailure { requestedChannels.remove(channelId) } // allow a later retry
            }
        }
    }

    fun toggle(gender: AthleteGender) = _state.update { it.copy(filters = it.filters.toggle(gender)) }
    fun selectCountry(country: String?) = _state.update { it.copy(filters = it.filters.withCountry(country)) }
    fun setQuery(query: String) = _state.update { it.copy(filters = it.filters.copy(query = query)) }
    fun toggleInactive() {
        val showing = !_state.value.filters.showInactive
        if (showing) ensureRetiredLoaded()
        _state.update { it.copy(filters = it.filters.copy(showInactive = showing)) }
    }
}
