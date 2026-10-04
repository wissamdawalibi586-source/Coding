plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // مترجم Compose (مطلوب مع Kotlin 2.0)
    alias(libs.plugins.ksp)            // يولّد كود Hilt وقت البناء
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.postsapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.postsapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Android الأساسي
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose: الـ BOM يوحّد إصدارات كل مكتبات Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ViewModel + جمع الـ StateFlow بوعي لدورة الحياة
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation بين الشاشات
    implementation(libs.androidx.navigation.compose)

    // Hilt: Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit: الاتصال بالـ API وتحويل JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // الاختبارات
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
