# Posts App: مشروع Android Native

تطبيق Android مبني بـ **Kotlin + Jetpack Compose + MVVM**. يجلب المنشورات من API حقيقي، ويعرضها في قائمة، وعند الضغط على منشور ينتقل إلى **صفحة التفاصيل**.

هذا هو المشروع النهائي (الأيام 11–14) من خطة التعلّم. الخطة الكاملة وقاموس المصطلحات في [`TASK_PLAN.md`](TASK_PLAN.md).

## التقنيات

| التقنية | الاستخدام |
|---|---|
| Kotlin | لغة البرمجة |
| Jetpack Compose + Material 3 | بناء الواجهات (Declarative UI) |
| ViewModel + StateFlow | معمارية MVVM وإدارة الحالة |
| Navigation Compose | التنقل بين القائمة والتفاصيل |
| Retrofit + Gson + OkHttp | جلب البيانات من الـ API |
| Hilt | Dependency Injection |
| JUnit + coroutines-test | اختبار الـ ViewModel |

API المستخدم: `https://jsonplaceholder.typicode.com/posts`

## التشغيل

1. افتح المجلد في **Android Studio** (Koala أو أحدث) عبر **File ← Open**.
2. انتظر انتهاء **Gradle Sync**، فهو يحمّل المكتبات تلقائياً.
3. شغّل Emulator أو وصّل جهازاً، ثم اضغط ▶️ **Run 'app'**.

الاختبارات من الـ Terminal:
```bash
./gradlew test          # Unit tests
./gradlew assembleDebug # بناء ملف APK
```

## بنية الملفات

```
app/src/main/java/com/example/postsapp/
├── PostsApplication.kt          ← @HiltAndroidApp: تهيئة Hilt عند بدء التطبيق
├── MainActivity.kt              ← Activity الوحيدة: setContent { AppNavGraph() }
│
├── data/                        ← طبقة Model (البيانات)
│   ├── model/Post.kt            ← شكل البيانات (data class)
│   ├── remote/PostApi.kt        ← Retrofit endpoints
│   └── repository/PostRepository.kt ← interface + التنفيذ الحقيقي
│
├── di/                          ← Hilt modules
│   ├── NetworkModule.kt         ← كيف يُنشأ OkHttp و Retrofit و PostApi
│   └── RepositoryModule.kt      ← ربط PostRepository ← PostRepositoryImpl
│
└── ui/                          ← طبقتا View و ViewModel
    ├── common/UiState.kt        ← Loading / Success / Error
    ├── common/StateViews.kt     ← LoadingView و ErrorView المشتركتان
    ├── list/                    ← شاشة القائمة + ViewModel الخاص بها
    ├── detail/                  ← شاشة التفاصيل + ViewModel الخاص بها
    ├── navigation/AppNavGraph.kt← المسارات (Routes) و NavHost
    └── theme/Theme.kt           ← الألوان والثيم

app/src/test/…/PostListViewModelTest.kt ← اختبارات بـ Fake Repository
```

## سير الكود باختصار

```
MainActivity.onCreate
  └─ AppNavGraph → NavHost(start = "posts")
       └─ PostListRoute → hiltViewModel<PostListViewModel>()
            └─ init { loadPosts() }               state = Loading   → LoadingView
                 └─ repository.getPosts()
                      └─ PostApi.getPosts()  (HTTP GET → JSON → List<Post>)
                 state = Success(posts)           → LazyColumn
                 أو state = Error(message)        → ErrorView + Retry
  ضغط على منشور → navigate("posts/5")
       └─ PostDetailRoute → PostDetailViewModel(SavedStateHandle["postId"] = 5)
            └─ repository.getPost(5) → Success(post) → PostContent
  زر الرجوع → popBackStack() → القائمة (الـ ViewModel ما زال يحمل البيانات، فلا يُعاد التحميل)
```

الشرح الكامل لكل خطوة موجود في القسم 4 من [`TASK_PLAN.md`](TASK_PLAN.md)، وفي التعليقات داخل كل ملف.

## قواعد المعمارية المتّبعة

- **View** (`*Screen.kt`) تعرض `UiState` فقط، وتُبلغ بالأحداث عبر lambdas مثل `onPostClick` و`onRetry`.
- **Route** (`*Route`) تربط الشاشة بالـ ViewModel، فتبقى الشاشة قابلة للعرض في `@Preview`.
- **ViewModel** لا يعرف Compose، ويتعامل مع `PostRepository` (interface) فقط.
- **Repository** هو المكان الوحيد الذي يعرف مصدر البيانات.
- تمرير الـ `id` فقط بين الشاشات، لا الكائن كاملاً.
