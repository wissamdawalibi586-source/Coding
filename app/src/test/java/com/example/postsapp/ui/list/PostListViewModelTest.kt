package com.example.postsapp.ui.list

import com.example.postsapp.MainDispatcherRule
import com.example.postsapp.data.model.Post
import com.example.postsapp.data.repository.PostRepository
import com.example.postsapp.ui.common.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Unit test للـ ViewModel دون إنترنت ودون Android:
 * نعطيه Repository وهمياً (Fake). هذه بالضبط فائدة الـ interface والـ Dependency Injection.
 */
class PostListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val samplePosts = listOf(
        Post(id = 1, userId = 1, title = "Title 1", body = "Body 1"),
        Post(id = 2, userId = 1, title = "Title 2", body = "Body 2"),
    )

    @Test
    fun `loads posts successfully on init`() {
        val viewModel = PostListViewModel(FakePostRepository(posts = samplePosts))

        assertEquals(UiState.Success(samplePosts), viewModel.state.value)
    }

    @Test
    fun `network failure shows error state`() {
        val viewModel = PostListViewModel(FakePostRepository(error = IOException()))

        assertTrue(viewModel.state.value is UiState.Error)
    }

    @Test
    fun `retry after failure loads posts`() {
        val repository = FakePostRepository(posts = samplePosts, error = IOException())
        val viewModel = PostListViewModel(repository)
        assertTrue(viewModel.state.value is UiState.Error)

        repository.error = null // "رجع الإنترنت"
        viewModel.loadPosts()

        assertEquals(UiState.Success(samplePosts), viewModel.state.value)
    }
}

private class FakePostRepository(
    private val posts: List<Post> = emptyList(),
    var error: Exception? = null,
) : PostRepository {

    override suspend fun getPosts(): List<Post> {
        error?.let { throw it }
        return posts
    }

    override suspend fun getPost(id: Int): Post {
        error?.let { throw it }
        return posts.first { it.id == id }
    }
}
