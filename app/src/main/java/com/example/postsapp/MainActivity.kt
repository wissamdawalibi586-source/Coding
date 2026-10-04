package com.example.postsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.postsapp.ui.navigation.AppNavGraph
import com.example.postsapp.ui.theme.PostsAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * نقطة الدخول الوحيدة للواجهة.
 *
 * دورة الحياة: onCreate → onStart → onResume → ... → onPause → onStop → onDestroy
 * نحتاج onCreate فقط: نضع فيه محتوى Compose، والباقي يديره NavHost والـ ViewModels.
 *
 * [AndroidEntryPoint] يسمح لـ Hilt بحقن الـ ViewModels في الشاشات داخل هذه الـ Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // setContent يقابل runApp() في Flutter
        setContent {
            PostsAppTheme {
                AppNavGraph()
            }
        }
    }
}
