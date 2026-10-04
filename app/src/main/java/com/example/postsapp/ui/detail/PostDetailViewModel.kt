package com.example.postsapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postsapp.data.model.Post
import com.example.postsapp.data.repository.PostRepository
import com.example.postsapp.ui.common.UiState
import com.example.postsapp.ui.common.toUserMessage
import com.example.postsapp.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel صفحة التفاصيل.
 *
 * [SavedStateHandle] يحمل الـ arguments القادمة من Navigation (هنا postId).
 * لهذا لا نمرّر الـ Post كاملاً بين الشاشتين، بل الـ id فقط، والـ ViewModel يجلب الباقي.
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PostRepository,
) : ViewModel() {

    private val postId: Int = checkNotNull(savedStateHandle[Routes.POST_ID_ARG])

    private val _state = MutableStateFlow<UiState<Post>>(UiState.Loading)
    val state: StateFlow<UiState<Post>> = _state.asStateFlow()

    init {
        loadPost()
    }

    fun loadPost() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(repository.getPost(postId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error(e.toUserMessage())
            }
        }
    }
}
