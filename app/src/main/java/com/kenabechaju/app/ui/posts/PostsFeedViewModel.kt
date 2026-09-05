package com.kenabechaju.app.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenabechaju.app.data.AuthResult
import com.kenabechaju.app.data.PostRepository
import com.kenabechaju.app.data.remote.dto.PostDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostsFeedUiState(
    val posts: List<PostDto> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
) {
    val hasMore: Boolean get() = posts.size < total
}

/** Follow-ranked feed, matching `GET /posts/feed`'s own ordering server-side
 *  (followed shops first) — the client just renders the order it gets. */
@HiltViewModel
class PostsFeedViewModel @Inject constructor(private val postRepository: PostRepository) : ViewModel() {
    private val _state = MutableStateFlow(PostsFeedUiState())
    val state: StateFlow<PostsFeedUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = postRepository.feed(offset = 0)) {
                is AuthResult.Success -> _state.value = _state.value.copy(isLoading = false, posts = result.value.items, total = result.value.total)
                is AuthResult.Failure -> _state.value = _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.isLoadingMore || s.isLoading || !s.hasMore) return
        _state.value = s.copy(isLoadingMore = true)
        viewModelScope.launch {
            when (val result = postRepository.feed(offset = s.posts.size)) {
                is AuthResult.Success -> {
                    val seen = s.posts.mapTo(mutableSetOf()) { it.id }
                    val merged = s.posts + result.value.items.filter { it.id !in seen }
                    _state.value = _state.value.copy(isLoadingMore = false, posts = merged, total = result.value.total)
                }
                is AuthResult.Failure -> _state.value = _state.value.copy(isLoadingMore = false)
            }
        }
    }
}
