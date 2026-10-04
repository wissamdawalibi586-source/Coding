package com.example.postsapp.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.postsapp.R
import com.example.postsapp.data.model.Post
import com.example.postsapp.ui.common.ErrorView
import com.example.postsapp.ui.common.LoadingView
import com.example.postsapp.ui.common.UiState
import com.example.postsapp.ui.theme.PostsAppTheme

/**
 * الطبقة الخارجية (Route): تربط الشاشة بالـ ViewModel.
 * hiltViewModel() يطلب من Hilt الـ ViewModel مع كل ما يحتاجه.
 */
@Composable
fun PostListRoute(
    onPostClick: (Int) -> Unit,
    viewModel: PostListViewModel = hiltViewModel(),
) {
    // نستمع للـ StateFlow. أي تغيير في الحالة يسبب Recomposition (إعادة رسم)
    val state by viewModel.state.collectAsStateWithLifecycle()
    PostListScreen(state = state, onPostClick = onPostClick, onRetry = viewModel::loadPosts)
}

/**
 * الشاشة نفسها (View): تعرض الحالة فقط ولا تعرف من أين جاءت البيانات.
 * فصلها عن الـ ViewModel يسمح برؤيتها في Preview دون شبكة.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    state: UiState<List<Post>>,
    onPostClick: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.posts_title)) }) },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        // Declarative UI: نصف ماذا يُعرض لكل حالة، والنظام يرسم
        when (state) {
            is UiState.Loading -> LoadingView(modifier)
            is UiState.Error -> ErrorView(state.message, onRetry, modifier)
            is UiState.Success -> PostList(state.data, onPostClick, modifier)
        }
    }
}

@Composable
private fun PostList(
    posts: List<Post>,
    onPostClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // LazyColumn = ListView.builder في Flutter: ترسم العناصر الظاهرة فقط
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // key: يساعد Compose على تتبّع كل عنصر بكفاءة عند تغيّر القائمة
        items(posts, key = { it.id }) { post ->
            PostItem(post = post, onClick = { onPostClick(post.id) })
        }
    }
}

@Composable
private fun PostItem(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostListScreenPreview() {
    PostsAppTheme {
        PostListScreen(
            state = UiState.Success(
                listOf(
                    Post(1, 1, "First post title", "Body of the first post"),
                    Post(2, 1, "Second post title", "Body of the second post"),
                )
            ),
            onPostClick = {},
            onRetry = {},
        )
    }
}
