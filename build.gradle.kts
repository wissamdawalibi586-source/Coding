// ملف البناء الجذري: يعلن الـ plugins فقط دون تطبيقها (apply false)،
// وكل module (هنا app فقط) يطبّق ما يحتاجه منها.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
