package com.example.postsapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.postsapp.ui.detail.PostDetailRoute
import com.example.postsapp.ui.list.PostListRoute

/** أسماء المسارات في مكان واحد حتى لا تتكرر النصوص في أماكن مختلفة. */
object Routes {
    const val POST_LIST = "posts"
    const val POST_ID_ARG = "postId"
    const val POST_DETAIL = "posts/{$POST_ID_ARG}"

    fun postDetail(id: Int) = "posts/$id"
}

/**
 * خريطة التنقل في التطبيق.
 * NavController = Navigator في Flutter، و NavHost = المكان الذي تُعرض فيه الشاشة الحالية.
 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.POST_LIST) {

        composable(Routes.POST_LIST) {
            PostListRoute(
                onPostClick = { id -> navController.navigate(Routes.postDetail(id)) },
            )
        }

        composable(
            route = Routes.POST_DETAIL,
            // نعرّف نوع الـ argument ليصل للـ ViewModel كـ Int عبر SavedStateHandle
            arguments = listOf(navArgument(Routes.POST_ID_ARG) { type = NavType.IntType }),
        ) {
            PostDetailRoute(onBack = { navController.popBackStack() })
        }
    }
}
