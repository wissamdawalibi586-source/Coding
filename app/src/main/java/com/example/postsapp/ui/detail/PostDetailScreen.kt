package com.example.postsapp.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

@Composable
fun PostDetailRoute(
    onBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PostDetailScreen(state = state, onBack = onBack, onRetry = viewModel::loadPost)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    state: UiState<Post>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.post_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // AutoMirrored: السهم ينقلب تلقائياً في اللغات من اليمين لليسار
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (state) {
            is UiState.Loading -> LoadingView(modifier)
            is UiState.Error -> ErrorView(state.message, onRetry, modifier)
            is UiState.Success -> PostContent(state.data, modifier)
        }
    }
}

@Composable
private fun PostContent(post: Post, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = post.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.post_author, post.userId),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(text = post.body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun PostDetailScreenPreview() {
    PostsAppTheme {
        PostDetailScreen(
            state = UiState.Success(Post(1, 7, "Post title", "The full body of the post.")),
            onBack = {},
            onRetry = {},
        )
    }
}
