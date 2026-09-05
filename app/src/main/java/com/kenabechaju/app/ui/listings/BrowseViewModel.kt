package com.kenabechaju.app.ui.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.BrowseFilters
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.remote.dto.CategoryDto
import com.kenabechaju.app.data.remote.dto.ListingDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowseUiState(
    val query: String = "",
    val categories: List<CategoryDto> = emptyList(),
    val selectedCategorySlug: String? = null,
    val sort: String = "newest",
    val listings: List<ListingDto> = emptyList(),
    val total: Int = 0,
    val savedIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
) {
    val hasMore: Boolean get() = listings.size < total
}

/** Home/browse grid — mirrors web's `app/listings/page.tsx`: debounced
 *  search (350ms, same as web), category chips, sort, infinite scroll. */
@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BrowseUiState())
    val state: StateFlow<BrowseUiState> = _state.asStateFlow()

    private var searchDebounceJob: Job? = null

    init {
        loadReferenceData()
        loadSavedIds()
        refresh()
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            when (val result = listingRepository.categories()) {
                is AuthResult.Success -> _state.value = _state.value.copy(categories = result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun loadSavedIds() {
        viewModelScope.launch {
            when (val result = listingRepository.savedIds()) {
                is AuthResult.Success -> _state.value = _state.value.copy(savedIds = result.value.toSet())
                is AuthResult.Failure -> Unit
            }
        }
    }

    fun onQueryChange(value: String) {
        _state.value = _state.value.copy(query = value)
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(350)
            refresh()
        }
    }

    fun onCategorySelected(slug: String?) {
        _state.value = _state.value.copy(selectedCategorySlug = slug)
        refresh()
    }

    fun onSortSelected(sort: String) {
        _state.value = _state.value.copy(sort = sort)
        refresh()
    }

    private fun currentFilters(offset: Int = 0): BrowseFilters {
        val s = _state.value
        return BrowseFilters(
            q = s.query.ifBlank { null },
            category = s.selectedCategorySlug,
            sort = s.sort,
            offset = offset,
        )
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = listingRepository.browse(currentFilters(0))) {
                is AuthResult.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    listings = result.value.items,
                    total = result.value.total,
                )
                is AuthResult.Failure -> _state.value =
                    _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.isLoadingMore || s.isLoading || !s.hasMore) return
        _state.value = s.copy(isLoadingMore = true)
        viewModelScope.launch {
            when (val result = listingRepository.browse(currentFilters(s.listings.size))) {
                is AuthResult.Success -> {
                    val seen = s.listings.mapTo(mutableSetOf()) { it.id }
                    val merged = s.listings + result.value.items.filter { it.id !in seen }
                    _state.value = _state.value.copy(isLoadingMore = false, listings = merged, total = result.value.total)
                }
                is AuthResult.Failure -> _state.value = _state.value.copy(isLoadingMore = false)
            }
        }
    }

    fun toggleSaved(listingId: String) {
        val current = _state.value.savedIds
        val optimistic = if (listingId in current) current - listingId else current + listingId
        _state.value = _state.value.copy(savedIds = optimistic)
        viewModelScope.launch {
            when (val result = listingRepository.toggleSaved(listingId)) {
                is AuthResult.Success -> {
                    val ids = _state.value.savedIds
                    _state.value = _state.value.copy(
                        savedIds = if (result.value) ids + listingId else ids - listingId,
                    )
                }
                is AuthResult.Failure -> _state.value = _state.value.copy(savedIds = current)
            }
        }
    }
}
