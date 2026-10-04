package com.example.fakestore

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [HiltAndroidApp] generates the app-wide dependency container.
 */
@HiltAndroidApp
class FakeStoreApp : Application()
