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

data class MyShopPostsUiState(
    val shopId: String = "",
    val posts: List<PostDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class MyShopPostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val shopId: String = checkNotNull(savedStateHandle["shopId"])

    private val _state = MutableStateFlow(MyShopPostsUiState(shopId = shopId))
    val state: StateFlow<MyShopPostsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            _state.value = when (val result = postRepository.myPosts(shopId)) {
                is AuthResult.Success -> _state.value.copy(isLoading = false, posts = result.value)
                is AuthResult.Failure -> _state.value.copy(isLoading = false, error = result.error.message)
            }
        }
    }

    fun delete(postId: String) {
        viewModelScope.launch {
            when (postRepository.deletePost(postId)) {
                is AuthResult.Success -> _state.value = _state.value.copy(posts = _state.value.posts.filter { it.id != postId })
                is AuthResult.Failure -> Unit
            }
        }
    }
}
