package com.kenabechaju.app.ui.posts

import androidx.lifecycle.SavedStateHandle
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

data class PostDetailUiState(
    val post: PostDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _state = MutableStateFlow(PostDetailUiState())
    val state: StateFlow<PostDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = postRepository.getPost(slug)) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, post = result.value)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }
}
