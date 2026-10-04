package com.example.postsapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * أول كلاس يُنشأ عند تشغيل التطبيق، قبل أي شاشة.
 * [HiltAndroidApp] يجعل Hilt ينشئ "حاوية" الـ dependencies على مستوى التطبيق كله.
 */
@HiltAndroidApp
class PostsApplication : Application()
