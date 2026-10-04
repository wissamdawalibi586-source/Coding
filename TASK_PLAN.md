# خطة مهمة Android Native (للمطوّر القادم من Flutter)

> المصدر: ملف `android_native_summary.pdf` الذي استلمته من المدير.
> الهدف: تعلّم Android Native خلال **أسبوعين**، ثم تسليم **مشروع كامل**: تطبيق يجلب بيانات من API، يعرضها في قائمة، وعند الضغط على عنصر ينتقل إلى **صفحة تفاصيل**.

---

## 1. ماذا تشمل المهمة؟

المهمة شقّان:

1. **التعلّم (الأسبوع الأول ومنتصف الثاني):** لغة Kotlin، ثم بناء الواجهات بـ Jetpack Compose، ثم تنظيم الكود بمعمارية MVVM.
2. **التطبيق العملي (الأيام 11–14):** مشروع حقيقي يجمع كل ما سبق. هذا ما سيُقيَّم عليه غالباً، ويجب أن تفهم **سير الكود** فيه خطوة بخطوة، لا أن يعمل فقط.

**ما سيبحث عنه المدير في المشروع:**

| المعيار | ماذا يعني عملياً |
|---|---|
| Kotlin سليم | `data class`، `null safety`، `coroutines` |
| واجهة Compose | `Composable`، `State`، `LazyColumn` |
| تنقّل بين شاشتين | `NavController` مع تمرير `id` للتفاصيل |
| MVVM واضح | View لا تعرف شيئاً عن الشبكة، و ViewModel لا يعرف شيئاً عن الواجهة |
| جلب بيانات من API | `Retrofit` + `coroutines` + معالجة الأخطاء |
| حالات الشاشة | Loading / Success / Error |
| (إضافي) Hilt | حقن الـ dependencies بدل إنشائها يدوياً |
| Git | commits صغيرة وواضحة |

---

## 2. الخطة الزمنية (أسبوعان)

### الأسبوع الأول
| الأيام | الموضوع | المخرَج المتوقع |
|---|---|---|
| 1–3 | أساسيات Kotlin عبر **Kotlin Koans** | حل تمارين: variables، functions، classes، collections، null safety، lambdas |
| 4–7 | **Jetpack Compose**: `Composable`، `State`، `LazyColumn`، `Navigation` | تطبيق صغير: قائمة ثابتة + شاشة تفاصيل |

### الأسبوع الثاني
| الأيام | الموضوع | المخرَج المتوقع |
|---|---|---|
| 8–10 | `ViewModel` + `StateFlow` + **MVVM** | نقل منطق التطبيق السابق إلى ViewModel |
| 11–14 | **المشروع النهائي** | تطبيق يجلب بيانات من API ويعرضها مع navigation لصفحة تفاصيل |

### تفصيل أيام المشروع (11–14)
- **اليوم 11:** إنشاء المشروع في Android Studio (Empty Compose Activity)، إضافة المكتبات في Gradle، بناء طبقة البيانات (Retrofit + Model + Repository).
- **اليوم 12:** ViewModel + `UiState` + شاشة القائمة.
- **اليوم 13:** Navigation + شاشة التفاصيل + معالجة الأخطاء والتحميل.
- **اليوم 14:** Hilt (اختياري)، تنظيف الكود، README، تجربة نهائية، رفع على GitHub.

---

## 3. بنية المشروع (منفّذة في هذا الريبو ✅)

المشروع الكامل موجود في هذا الريبو، وطريقة التشغيل والبنية التفصيلية في [`README.md`](README.md).
API المستخدم: `https://jsonplaceholder.typicode.com/posts`

| الملف | الطبقة | الدور |
|---|---|---|
| `PostsApplication.kt` | تهيئة | `@HiltAndroidApp` |
| `MainActivity.kt` | نقطة الدخول | `setContent { AppNavGraph() }` |
| `data/model/Post.kt` | Model | شكل البيانات |
| `data/remote/PostApi.kt` | Model | Retrofit endpoints |
| `data/repository/PostRepository.kt` | Model | interface + `PostRepositoryImpl` |
| `di/NetworkModule.kt` · `di/RepositoryModule.kt` | DI | وصفات Hilt |
| `ui/common/UiState.kt` · `StateViews.kt` | مشترك | الحالات + Loading/Error views |
| `ui/list/PostListViewModel.kt` · `PostListScreen.kt` | ViewModel / View | شاشة القائمة |
| `ui/detail/PostDetailViewModel.kt` · `PostDetailScreen.kt` | ViewModel / View | شاشة التفاصيل |
| `ui/navigation/AppNavGraph.kt` | Navigation | `Routes` + `NavHost` |
| `app/src/test/.../PostListViewModelTest.kt` | Tests | 3 اختبارات بـ Fake Repository |

> الأمثلة في القسم 4 أدناه **مبسّطة** للشرح. النسخة الكاملة في الملفات أعلاه، وفيها إضافات احترافية:
> فصل `Route` عن `Screen` لتعمل `@Preview`، ومعالجة `CancellationException`، ورسائل خطأ مفهومة، و`Repository` على شكل interface لتسهيل الاختبار.

---

## 4. سير الكود خطوة بخطوة (Code Flow)

ماذا يحدث من لحظة فتح التطبيق حتى ظهور البيانات؟

```
المستخدم يفتح التطبيق
   ↓
MainActivity.onCreate()  →  setContent { AppNavGraph() }
   ↓
NavHost يعرض الشاشة الأولى: PostListScreen
   ↓
PostListScreen تطلب PostListViewModel
   ↓
ViewModel في init{} يطلق coroutine:  repository.getPosts()
   ↓  (الحالة الآن: Loading → الشاشة تعرض دائرة تحميل)
Repository يستدعي  api.getPosts()  (Retrofit)
   ↓
Retrofit يرسل طلب HTTP GET → السيرفر يرد بـ JSON
   ↓
محوّل JSON (Gson/Moshi) يحوّل الرد إلى List<Post>
   ↓
ViewModel يحدّث StateFlow إلى Success(posts)   (أو Error عند الفشل)
   ↓
Compose يلاحظ تغيّر الحالة → Recomposition → LazyColumn ترسم القائمة
   ↓
المستخدم يضغط على عنصر  →  navController.navigate("detail/5")
   ↓
PostDetailScreen + PostDetailViewModel يجلبان المنشور رقم 5 بنفس الدورة
```

### الخطوة 1: Model (شكل البيانات)
```kotlin
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)
```
`data class` تولّد تلقائياً `equals` و`toString` و`copy`. أسماء الحقول تطابق مفاتيح الـ JSON.

### الخطوة 2: Retrofit API (الاتصال بالإنترنت)
```kotlin
interface PostApi {
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post
}
```
تكتب فقط **واجهة** (interface)، وRetrofit ينشئ الكود الفعلي. `suspend` تعني أن الدالة تعمل داخل coroutine فلا تجمّد الواجهة.

### الخطوة 3: Repository (مصدر الحقيقة)
```kotlin
class PostRepository(private val api: PostApi) {
    suspend fun getPosts(): List<Post> = api.getPosts()
    suspend fun getPost(id: Int): Post = api.getPost(id)
}
```
الـ ViewModel لا يعرف إن كانت البيانات من الإنترنت أو من Room. إن أضفت تخزيناً محلياً لاحقاً، تغيّر الـ Repository فقط.

### الخطوة 4: UiState + ViewModel (المدير)
```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class PostListViewModel(private val repository: PostRepository) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Post>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Post>>> = _state.asStateFlow()

    init { loadPosts() }

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(repository.getPosts())
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```
- `_state` خاص وقابل للتعديل، و`state` عام وللقراءة فقط. الشاشة **تقرأ** ولا **تعدّل**.
- `viewModelScope` يُلغي الـ coroutine تلقائياً عند تدمير الـ ViewModel.
- الـ ViewModel **يبقى حياً عند تدوير الشاشة**، فلا يُعاد الطلب.

### الخطوة 5: View (الشاشة)
```kotlin
@Composable
fun PostListScreen(
    viewModel: PostListViewModel,
    onPostClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is UiState.Loading -> CircularProgressIndicator()
        is UiState.Error   -> Button(onClick = viewModel::loadPosts) { Text("Retry: ${s.message}") }
        is UiState.Success -> LazyColumn {
            items(s.data, key = { it.id }) { post ->
                Text(
                    text = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPostClick(post.id) }
                        .padding(16.dp)
                )
            }
        }
    }
}
```
الشاشة **تصف** ما يجب عرضه لكل حالة (Declarative)، ولا تعرف شيئاً عن Retrofit. عند الضغط **تُبلغ** فقط (`onPostClick`) ولا تتنقل بنفسها.

### الخطوة 6: Navigation (التنقّل)
```kotlin
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "list") {
        composable("list") {
            PostListScreen(
                viewModel = hiltViewModel(),
                onPostClick = { id -> navController.navigate("detail/$id") }
            )
        }
        composable(
            "detail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) {
            PostDetailScreen(viewModel = hiltViewModel())
        }
    }
}
```
نمرّر الـ `id` فقط، لا الكائن كاملاً. يقرأه `PostDetailViewModel` من `SavedStateHandle` ويجلب التفاصيل بنفسه.

### الخطوة 7: Hilt (حقن الـ dependencies)، اختياري لكنه احترافي
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideApi(): PostApi = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PostApi::class.java)

    @Provides @Singleton
    fun provideRepository(api: PostApi) = PostRepository(api)
}
```
ثم تضع `@HiltAndroidApp` على كلاس `Application`، و`@AndroidEntryPoint` على `MainActivity`، و`@HiltViewModel` مع `@Inject constructor` على كل ViewModel.

### الخطوة 8: صلاحية الإنترنت
في `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
نسيان هذا السطر هو الخطأ الأشهر عند المبتدئين.

---

## 5. قاموس المصطلحات (English → عربي)

### اللغات
| المصطلح | المعنى |
|---|---|
| **Java** | اللغة الأصلية لـ Android، وما زالت موجودة في مشاريع قديمة كثيرة |
| **Kotlin** | اللغة الرسمية الحديثة لـ Android منذ 2017، أقصر وأأمن من Java |
| **OOP (Object Oriented Programming)** | البرمجة كائنية التوجه: تنظيم الكود في كلاسات وكائنات (وراثة، تغليف، تعدد أشكال) |
| **Variable (`val` / `var`)** | متغيّر. `val` ثابت لا يتغيّر بعد إسناده، و`var` قابل للتغيير |
| **Null Safety** | حماية من خطأ القيمة الفارغة. `String?` تقبل null و`String` لا تقبلها |
| **data class** | كلاس مخصص لحمل البيانات، يولّد دوال المقارنة والطباعة تلقائياً |
| **sealed class / interface** | مجموعة مغلقة من الأنواع المحتملة (مثل Loading/Success/Error)، فيُجبرك `when` على تغطيتها كلها |
| **Lambda** | دالة مجهولة تُمرَّر كقيمة: `{ id -> navigate(id) }` |
| **Coroutine** | تنفيذ عمل طويل (كطلب شبكة) دون تجميد الواجهة. يقابل `async/await` في Dart |
| **suspend function** | دالة يمكن "إيقافها مؤقتاً" دون حجز الـ thread، ولا تُستدعى إلا من coroutine |

### الواجهات (UI)
| المصطلح | المعنى |
|---|---|
| **UI (User Interface)** | واجهة المستخدم: كل ما يراه ويلمسه |
| **XML Layout** | الطريقة القديمة: وصف الشاشة في ملف منفصل عن الكود (يشبه HTML) |
| **Jetpack Compose** | الطريقة الحديثة (2021): كتابة الواجهة بـ Kotlin مباشرة |
| **Declarative UI** | تصف **كيف يجب أن تبدو** الشاشة لكل حالة، والنظام يتكفل بالرسم (مثل Flutter) |
| **Composable** | دالة عليها `@Composable` ترسم جزءاً من الواجهة. يقابل **Widget** في Flutter |
| **Column / Row / Box** | ترتيب العناصر عمودياً / أفقياً / فوق بعضها |
| **LazyColumn** | قائمة ترسم العناصر الظاهرة فقط. يقابل `ListView.builder` |
| **Modifier** | سلسلة تعديلات على العنصر: حجم، padding، ضغط، لون… |
| **State** | قيمة إذا تغيّرت يُعاد رسم الواجهة. `remember { mutableStateOf(...) }` |
| **Recomposition** | إعادة استدعاء الـ Composable عند تغيّر الـ State. يقابل `setState`/`build` |
| **Navigation / NavController / NavHost** | نظام التنقل بين الشاشات. `NavController` يقابل `Navigator` في Flutter |
| **Route** | اسم/مسار الشاشة مثل `"detail/{postId}"` |

### المعمارية
| المصطلح | المعنى |
|---|---|
| **Architecture** | طريقة تنظيم الكود في طبقات لكل منها مسؤولية |
| **MVVM** | Model – View – ViewModel |
| **Model** | البيانات + منطق العمل. يتواصل مع الـ API أو قاعدة البيانات (المطبخ 🍳) |
| **View** | الشاشة: تعرض فقط وتُبلغ بأفعال المستخدم (الزبون 🧑) |
| **ViewModel** | الوسيط: يجلب البيانات ويحضّرها ويحتفظ بها عند تدوير الشاشة (النادل 🧑‍🍳) |
| **Business Logic** | قواعد عمل التطبيق (حسابات، تحقق، قرارات)، وليس لها علاقة بالشكل |
| **Repository** | طبقة تخفي مصدر البيانات (شبكة/محلي) عن الـ ViewModel |
| **UiState** | كائن واحد يصف كل ما تحتاجه الشاشة في لحظة معينة |
| **StateFlow** | تدفّق بيانات يحمل دائماً آخر قيمة، والشاشة "تستمع" له. يقابل `ValueNotifier`/`Stream` |
| **viewModelScope** | نطاق coroutines مرتبط بعمر الـ ViewModel، ويُلغى تلقائياً |
| **Single Source of Truth** | مكان واحد فقط يملك البيانات الصحيحة |
| **Unidirectional Data Flow** | البيانات تنزل من ViewModel إلى View، والأحداث تصعد من View إلى ViewModel |

### مكونات Android
| المصطلح | المعنى |
|---|---|
| **Activity** | شاشة كاملة. في Compose الحديث تكفي غالباً Activity واحدة |
| **Fragment** | جزء من شاشة قابل لإعادة الاستخدام (أسلوب XML القديم غالباً) |
| **Intent** | رسالة للانتقال بين الشاشات أو فتح تطبيق آخر وتمرير بيانات |
| **Lifecycle** | دورة حياة الشاشة: `onCreate → onStart → onResume → onPause → onStop → onDestroy` |
| **onCreate** | الشاشة تُنشأ: هنا نستدعي `setContent {}` |
| **onResume / onPause** | الشاشة أصبحت تفاعلية / فقدت التركيز |
| **onDestroy** | الشاشة تُدمَّر (مثلاً عند تدوير الجهاز)، لذلك نحفظ البيانات في ViewModel |
| **AndroidManifest.xml** | بطاقة هوية التطبيق: الشاشات والصلاحيات (مثل INTERNET) |
| **Permission** | صلاحية يطلبها التطبيق (إنترنت، كاميرا…) |

### التخزين والشبكة
| المصطلح | المعنى |
|---|---|
| **SharedPreferences / DataStore** | تخزين قيم بسيطة (إعدادات، توكن). `DataStore` هو البديل الحديث |
| **SQLite** | قاعدة بيانات محلية داخل الجهاز |
| **Room** | مكتبة فوق SQLite تسهّلها: `@Entity` و`@Dao` و`@Database` |
| **API** | واجهة يقدّمها سيرفر لتطلب منه بيانات |
| **REST / Endpoint** | أسلوب تصميم API، والـ endpoint هو عنوان محدد مثل `/posts/1` |
| **HTTP GET / POST** | جلب بيانات / إرسال بيانات |
| **JSON** | صيغة نصية لنقل البيانات `{ "id": 1, "title": "..." }` |
| **Retrofit** | مكتبة تحوّل interface إلى طلبات HTTP جاهزة |
| **Converter (Gson / Moshi / kotlinx.serialization)** | يحوّل JSON إلى كائنات Kotlin والعكس |

### الأدوات
| المصطلح | المعنى |
|---|---|
| **IDE** | بيئة التطوير المتكاملة |
| **Android Studio** | الـ IDE الرسمي: محرر + emulator + layout inspector + debugger |
| **Emulator** | جهاز Android وهمي على الكمبيوتر |
| **Debugging / Breakpoint** | تتبّع الأخطاء بإيقاف الكود عند سطر معيّن وفحص القيم |
| **Logcat** | نافذة سجلات التطبيق والأخطاء في Android Studio |
| **Gradle** | نظام البناء وإدارة المكتبات. يقابل `pubspec.yaml` و`pub` في Flutter |
| **Dependency / Library** | مكتبة خارجية يعتمد عليها مشروعك |
| **Version Catalog (`libs.versions.toml`)** | ملف مركزي لإصدارات المكتبات في المشاريع الحديثة |
| **Git / Commit / Branch / Push** | التحكم بالنسخ: حفظ لقطة / فرع عمل مستقل / رفع للسيرفر |
| **Dependency Injection (DI)** | بدل أن ينشئ الكلاس ما يحتاجه بنفسه، **يُعطى** له من الخارج، فيسهل اختباره وتبديله |
| **Hilt** | مكتبة DI الرسمية من Google: `@HiltAndroidApp`، `@AndroidEntryPoint`، `@HiltViewModel`، `@Inject`، `@Module`، `@Provides` |
| **Singleton** | نسخة واحدة فقط من الكائن في التطبيق كله |

---

## 6. مقارنة سريعة Flutter ↔ Android Native

| Flutter | Android (Compose) |
|---|---|
| Dart | Kotlin |
| Widget | `@Composable` function |
| `Column` / `Row` | `Column` / `Row` |
| `ListView.builder` | `LazyColumn` |
| `Navigator` | `NavController` |
| `setState` | `mutableStateOf` + Recomposition |
| Provider / Bloc / Riverpod | ViewModel + StateFlow |
| `http` / `dio` | Retrofit |
| `sqflite` / `drift` | Room |
| `shared_preferences` | DataStore / SharedPreferences |
| `pubspec.yaml` | `build.gradle.kts` |
| `get_it` / `injectable` | Hilt |
| `async` / `await` / `Future` | `suspend` / coroutines |

> بحسب الملف: إذا كانت لديك خلفية Flutter فأنت تفهم نحو 60% من Compose مسبقاً.

---

## 7. مصادر التعلّم (من الملف)

| المصدر | لماذا |
|---|---|
| **Philipp Lackner** (YouTube) | الأفضل لـ Kotlin + Compose + MVVM، عملي ومحدّث |
| **Google Codelabs**: developer.android.com/codelabs | رسمي ومجاني وخطوة بخطوة (ابدأ بـ *Android Basics with Compose*) |
| **Kotlin Koans**: play.kotlinlang.org/koans | تمارين تفاعلية لتعلّم Kotlin بسرعة |
| **Tim Buchalka** (Udemy) | إذا اشترطت الشركة Java |

---

## 8. سير العمل الاحترافي مع Claude Code (الـ Skills)

| المرحلة | الأداة/الـ Skill | الاستخدام في هذا المشروع |
|---|---|---|
| بداية المشروع | `/init` | بعد إنشاء مشروع Android، يولّد `CLAUDE.md` يوثّق البنية والأوامر |
| تجهيز البيئة | `/session-start-hook` | يضبط تثبيت JDK/Gradle تلقائياً ليعمل `./gradlew test` في الجلسات السحابية |
| التخطيط | Plan mode | قبل كل ميزة كبيرة (Hilt، Room) |
| التحقق | `./gradlew build`، `./gradlew test`، `./gradlew lint` | بعد كل خطوة |
| مراجعة الأخطاء | `/code-review` | قبل كل commit مهم |
| تحسين الجودة | `/simplify` | بعد أن يعمل الكود |
| الأمان | `/security-review` | إذا أضفت تسجيل دخول أو API keys |

**قاعدة ذهبية:** افهم أولاً، ثم نفّذ، ثم تحقق، ثم راجع، ثم اعمل commit.

---

## 9. قائمة التحقق قبل التسليم ✅

- [ ] التطبيق يعمل على Emulator دون crash
- [ ] شاشة قائمة تجلب البيانات من API حقيقي
- [ ] الضغط على عنصر يفتح صفحة التفاصيل بالـ `id` الصحيح
- [ ] زر الرجوع يعيد للقائمة دون إعادة تحميل غير ضرورية
- [ ] حالات Loading و Error (مع زر Retry) تعمل. جرّب بإطفاء الإنترنت
- [ ] تدوير الشاشة لا يعيد الطلب (ViewModel يحتفظ بالبيانات)
- [ ] صلاحية `INTERNET` موجودة في Manifest
- [ ] View لا تحتوي أي استدعاء لـ Retrofit أو Repository
- [ ] لا توجد قيم ثابتة مكررة (Base URL في مكان واحد)
- [ ] (إضافي) Hilt بدل الإنشاء اليدوي
- [ ] (إضافي) Unit test واحد على الأقل للـ ViewModel
- [ ] README يشرح المعمارية وكيفية التشغيل
- [ ] commits واضحة على GitHub

---

## 10. أسئلة متوقعة في المقابلة / المراجعة

1. **لماذا ViewModel وليس حفظ البيانات في الـ Activity؟** لأن الـ Activity تُدمَّر عند تدوير الشاشة، والـ ViewModel يبقى.
2. **ما الفرق بين `StateFlow` و `LiveData`؟** كلاهما قابل للمراقبة. `StateFlow` من Kotlin coroutines ولا يرتبط بـ Android، وهو المفضّل مع Compose.
3. **لماذا Repository؟** لفصل مصدر البيانات وتسهيل الاختبار وإمكانية إضافة cache لاحقاً.
4. **ما Recomposition؟** إعادة رسم الأجزاء التي تغيّرت حالتها فقط.
5. **لماذا `suspend`؟** حتى لا يُنفَّذ طلب الشبكة على الـ Main thread فيتجمّد التطبيق.
6. **ما فائدة Dependency Injection؟** تقليل الترابط وتسهيل الاختبار باستبدال الـ API الحقيقي بـ Fake.
7. **ماذا يحدث لو نسيت صلاحية INTERNET؟** يفشل الطلب بـ `SecurityException` أو `UnknownHostException`.
