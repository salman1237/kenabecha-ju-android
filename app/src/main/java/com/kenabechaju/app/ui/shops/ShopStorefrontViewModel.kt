package com.kenabechaju.app.ui.shops

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.BrowseFilters
import com.kenabechaju.app.data.ListingRepository
import com.kenabechaju.app.data.PostRepository
import com.kenabechaju.app.data.ShopRepository
import com.kenabechaju.app.data.remote.dto.ListingDto
import com.kenabechaju.app.data.remote.dto.PostDto
import com.kenabechaju.app.data.remote.dto.RatingDto
import com.kenabechaju.app.data.remote.dto.ShopDto
import com.kenabechaju.app.data.remote.dto.ShopStatsDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopStorefrontUiState(
    val shop: ShopDto? = null,
    val stats: ShopStatsDto? = null,
    val listings: List<ListingDto> = emptyList(),
    val reviews: List<RatingDto> = emptyList(),
    val posts: List<PostDto> = emptyList(),
    val isLoading: Boolean = true,
    val isFollowLoading: Boolean = false,
    val error: String? = null,
)

/** Web's shop storefront: branding, listings (manual seller order), a
 *  reviews tab, follow/unfollow, and the stats block (active/sold/
 *  followers/rating + the "you can rate this shop" prompt — the prompt
 *  is shown, but writing a review is Phase 6 territory). */
@HiltViewModel
class ShopStorefrontViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val listingRepository: ListingRepository,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _state = MutableStateFlow(ShopStorefrontUiState())
    val state: StateFlow<ShopStorefrontUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = shopRepository.getShop(slug)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, shop = result.value)
                    loadStats()
                    loadListings(result.value.id)
                    loadReviews()
                    loadPosts(result.value.id)
                }
                is AuthResult.Failure -> _state.value = _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            when (val result = shopRepository.getShopStats(slug)) {
                is AuthResult.Success -> _state.value = _state.value.copy(stats = result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun loadListings(shopId: String) {
        viewModelScope.launch {
            when (val result = listingRepository.browse(BrowseFilters(shopId = shopId, sort = "manual", limit = 50))) {
                is AuthResult.Success -> _state.value = _state.value.copy(listings = result.value.items)
                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            when (val result = shopRepository.getShopReviews(slug)) {
                is AuthResult.Success -> _state.value = _state.value.copy(reviews = result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun loadPosts(shopId: String) {
        viewModelScope.launch {
            when (val result = postRepository.shopPosts(shopId)) {
                is AuthResult.Success -> _state.value = _state.value.copy(posts = result.value)
                is AuthResult.Failure -> Unit
            }
        }
    }

    fun toggleFollow() {
        val shopId = _state.value.shop?.id ?: return
        val currentlyFollowing = _state.value.stats?.isFollowing ?: return
        _state.value = _state.value.copy(isFollowLoading = true)
        viewModelScope.launch {
            when (val result = shopRepository.toggleFollow(shopId)) {
                is AuthResult.Success -> _state.value = _state.value.copy(
                    isFollowLoading = false,
                    stats = _state.value.stats?.copy(isFollowing = result.value),
                )
                is AuthResult.Failure -> _state.value = _state.value.copy(isFollowLoading = false, error = result.error.message)
            }
        }
    }
}
