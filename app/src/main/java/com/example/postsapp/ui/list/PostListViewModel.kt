package com.example.postsapp.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postsapp.data.model.Post
import com.example.postsapp.data.repository.PostRepository
import com.example.postsapp.ui.common.UiState
import com.example.postsapp.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel شاشة القائمة: "النادل" بين الشاشة والبيانات.
 *
 * - يجلب البيانات من [PostRepository] ويحوّلها إلى [UiState].
 * - يبقى حياً عند تدوير الشاشة، فلا يُعاد طلب الشبكة.
 * - لا يعرف شيئاً عن Compose ولا عن الشاشة نفسها.
 */
@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    // _state خاص وقابل للتعديل، و state عام للقراءة فقط: الشاشة تقرأ ولا تعدّل
    private val _state = MutableStateFlow<UiState<List<Post>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Post>>> = _state.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        // viewModelScope: يُلغى تلقائياً عند تدمير الـ ViewModel، فلا تتسرّب الذاكرة
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(repository.getPosts())
            } catch (e: CancellationException) {
                throw e // الإلغاء ليس خطأً، فيجب تمريره وإلا تتعطل الـ coroutines
            } catch (e: Exception) {
                UiState.Error(e.toUserMessage())
            }
        }
    }
}
