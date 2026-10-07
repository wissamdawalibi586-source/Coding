# شرح كود المشروع ملفاً ملفاً

## 0. قبل البدء: كيف تنقل المشروع إلى Android Studio

### الطريقة الأسهل (موصى بها)
1. حمّل الريبو من GitHub كملف ZIP، أو استخدم `git clone`.
2. افتح Android Studio، ثم اختر **File ← Open** وحدد مجلد المشروع.
3. انتظر انتهاء **Gradle Sync**، فهو يحمّل المكتبات تلقائياً.
4. اضغط ▶️ **Run**.

### إذا أردت نسخ الملفات يدوياً إلى مشروع جديد
1. أنشئ مشروعاً جديداً من قالب **Empty Views Activity**.
2. اجعل اسم الحزمة **Package name** يساوي `com.example.fakestore`، واللغة **Kotlin**، و**Minimum SDK** يساوي 24.
3. انسخ الملفات بنفس المسارات المكتوبة في عنوان كل قسم من هذا الملف.
4. استبدل ملفات Gradle الموجودة بالملفات الواردة هنا، ثم اضغط **Sync Now**.

### بنية المشروع
```
app/src/main/java/com/example/fakestore/
├── FakeStoreApp.kt                 Application + Hilt
├── MainActivity.kt                 the only Activity
├── data/
│   ├── model/Product.kt            domain model
│   ├── remote/                     Retrofit APIs + DTOs
│   ├── auth/                       ★ token logic (core of the task)
│   ├── mock/                       dev-only mock server (FakeStore outage)
│   └── repository/                 ProductRepository
├── di/                             Hilt modules
└── ui/
    ├── common/                     UiState, errors, helpers
    ├── login/                      Login screen
    ├── products/                   list (RecyclerView)
    └── detail/                     product details
app/src/main/res/                   layouts, navigation, menu, strings
app/src/test/.../data/auth/         unit tests for the token logic
app/src/test/.../data/mock/         tests on the mock server
```

### ترتيب القراءة المقترح
اقرأ الأقسام بالترتيب، فكل قسم يعتمد على ما قبله:

| الجزء | المحتوى |
|---|---|
| التمهيدي ★ | **دليل الدراسة:** ماذا تقرأ أولاً، وإلى أي عمق |
| الأول | إعدادات Gradle والـ Manifest |
| الثاني | نماذج البيانات والـ APIs |
| الثالث ★ | منطق الـ Token، وهو قلب المهمة |
| الرابع | الـ Repository وحقن الاعتماديات (Hilt) |
| الخامس | أدوات الواجهة المشتركة |
| السادس | الشاشات |
| السابع | الاختبارات |
| الثامن | الرحلة الكاملة للطلب، وكيف تعرض المشروع للمدير |
| التاسع | الوضع التجريبي (السيرفر الوهمي) أثناء تعطّل FakeStore |
| العاشر ★ | **فهم المنطق بالسيناريوهات**، ودليل اختبار التطبيق على الهاتف، ونتائج المراجعة |

> **قبل أن تبدأ:** اقرأ "الجزء التمهيدي: دليل الدراسة" التالي مباشرة. فيه خطة من أربع جلسات تقول لك ماذا تقرأ أولاً، وما الذي يمكن تخطّيه.

> **ملاحظة:** أرقام الأسطر الظاهرة بجانب الكود تساعدك على متابعة الشرح.

### سجل التعديلات
المشروع مرّ بخمس مراحل، وكلها مشروحة في هذا الملف:

| المرحلة | ماذا أُضيف | لماذا | الأقسام |
|---|---|---|---|
| 1. التنفيذ الأساسي | كل متطلبات المهمة: الدخول، والتخزين المشفّر، والـ Interceptor، والانتهاء بعد 60 ثانية، والتجديد الواحد، والخروج الإجباري، والقائمة، والتفاصيل، والخروج + 15 اختباراً | ملف المهمة | 1 إلى 56 |
| 2. رسالة "السيرفر غير متاح" | نوع خطأ جديد `ServerUnavailableException`، ورسالة دقيقة عند أعطال السيرفر (5xx)، واختباران | FakeStore تعطّل، والتطبيق كان يقول "لا يوجد إنترنت"، وهذا غير دقيق | 17، 20، 29، 51 |
| 3. الوضع التجريبي | سيرفر وهمي لنسخة التطوير فقط، يُفعَّل من `gradle.properties`، وشريط تنبيه في شاشة الدخول، و4 اختبارات | FakeStore بقي متعطّلاً للجميع، فلم يكن ممكناً تشغيل التطبيق أو عرضه | 4، 5، 26، 38، 39، الجزء التاسع |
| 4. المراجعة الشاملة | قفل `@Synchronized` في `EncryptedTokenStorage`، وحقل اسم مستخدم بلا تكبير تلقائي، وتوضيح حدود السيرفر الوهمي، والجزء العاشر كاملاً | مراجعة كل الملفات قبل التسليم | 16، 38، 55، الجزء التاسع، الجزء العاشر |
| 5. دليل الدراسة | الجزء التمهيدي: مستويات الأهمية، وخطة أربع جلسات، وقاموس Flutter ← Android، وقاعدة "لو حذفنا هذا السطر" | تسهيل الدراسة قبل المقابلة | الجزء التمهيدي |

**المجموع الحالي:** 56 ملفاً مشروحاً، و**21 اختباراً ناجحاً**.

---

## الجزء التمهيدي ★: دليل الدراسة

هذا الجزء لا يشرح كوداً. يقول لك **ماذا تقرأ، وبأي ترتيب، وإلى أي عمق**، حتى تفهم المشروع بسرعة ودون أن تضيع في التفاصيل.

### أ) ثلاثة مستويات للأهمية
ليس كل ملف بنفس الأهمية. وزّع وقتك هكذا:

| المستوى | المعنى | الأقسام |
|---|---|---|
| 🔴 افهمه سطراً سطراً | قلب المهمة، والمدير سيسأل عنه حتماً | 17، 20، 21، 22، 25، 26، 61، 62 |
| 🟡 افهم الفكرة فقط | اعرف **ماذا** يفعل و**لماذا** نحتاجه، دون كل التفاصيل | 14، 16، 24، 27، 29، 35، 36، 37، 41، 43، 46، 58 |
| 🟢 مرّ عليه بسرعة | إعدادات وتصميم، لا يُسأل عنها عادة | 1 إلى 12، 23، 28، 30 إلى 34، 38 إلى 40، 42، 44، 45، 47 إلى 49، 57 |

**الفرق المهم:**

- "لا أعرف **كيف** تعمل مكتبة Retrofit من الداخل": طبيعي، ولا أحد يسألك عنه.
- "لا أعرف **لماذا** كتبنا هذا السطر في `AuthManager`": خطر، لأن المدير يفترض أنك أنت من كتبه.

### ب) خطة الدراسة: أربع جلسات
كل جلسة ساعة تقريباً. لا تنتقل للجلسة التالية حتى تجيب عن أسئلة نهاية الجلسة **دون النظر للملف**.

| الجلسة | ماذا تقرأ | الهدف |
|---|---|---|
| 1. الصورة الكاملة | 61، ثم 62، ثم 54 | تفهم القصة دون كود |
| 2. قلب المهمة 🔴 | 17، 20 (ببطء)، 21، 22، ثم 25 و26 | تفهم الكود الذي ينفّذ القصة |
| 3. الواجهة وHilt 🟡 | مقدمة الجزء السادس، ثم 36، 37، 41، 43، ثم 24 و27 | تفهم كيف تصل البيانات للشاشة |
| 4. الإثبات والعرض | 51، 52، 53، ثم 55، ثم 63 | تعرف كيف تثبت أن الكود صحيح وكيف تعرضه |

**أسئلة نهاية كل جلسة:**

| الجلسة | أجب بصوت عالٍ |
|---|---|
| 1 | ماذا يحدث عندما ترسل 5 طلبات معاً والـ token منتهٍ؟ ومتى نُخرج المستخدم إجبارياً، ومتى لا؟ |
| 2 | لماذا يوجد تحقق ثانٍ داخل القفل؟ ولماذا عميلا OkHttp؟ ولماذا ترث الأخطاء من `IOException`؟ |
| 3 | من أين يأتي `AuthManager` إلى `LoginViewModel`؟ وكيف تعرف الشاشة أن البيانات وصلت؟ |
| 4 | كيف أثبتنا أن التجديد يحدث مرة واحدة فقط؟ وكيف نختبر 60 ثانية دون انتظار؟ |

### ج) قاعدة "لو حذفنا هذا السطر"
أفضل طريقة لتعرف أنك فهمت سطراً: اسأل نفسك **ماذا سيحدث لو حذفناه؟** هذه إجابات أهم الأسطر في المشروع:

| لو حذفنا… | الملف | ماذا سيحدث |
|---|---|---|
| التحقق الثاني داخل القفل (السطر 78) | `AuthManager` | 5 طلبات متزامنة تنتج **5** طلبات login بدل واحد |
| `synchronized(refreshLock)` | `AuthManager` | الطلبات تجدّد كلها في نفس اللحظة، دون أي انتظار |
| `@Singleton` | `AuthManager` | كل كلاس يحصل على نسخة خاصة به، بقفل خاص. فيفشل منع التجديد المكرر، ولا تصل أحداث الخروج إلى `MainActivity` |
| فحص رقم الجيل في `saveIfStillCurrent` | `AuthManager` | تجديد بدأ قبل Logout يعيد المستخدم **مسجّلاً للدخول** بعد أن خرج |
| العميل المنفصل `@PlainClient` | `NetworkModule` | طلب login يمر بـ `AuthInterceptor`، فيطلب تجديداً، والتجديد هو login: **حلقة لا نهائية** |
| `extends IOException` | `AuthExceptions` | رمي الخطأ من داخل OkHttp **يُسقط التطبيق** (crash) بدل عرض رسالة |
| فحص `priorResponse` | `TokenAuthenticator` | سيرفر يرد 401 دائماً يسبّب حتى 20 محاولة متتالية |
| `@Synchronized` | `EncryptedTokenStorage` | قد يُقرأ token قديم مع وقت حفظ جديد، فيُعامل الـ token المنتهي كأنه صالح |
| `popUpTo` في `action_login_to_products` | `nav_graph.xml` | زر الرجوع من المنتجات يعيد المستخدم لشاشة Login وهو مسجّل الدخول |
| `throw e` في `catch (CancellationException)` | الـ ViewModels | تتعطل آلية إلغاء الـ coroutines عند إغلاق الشاشة |
| `_binding = null` في `onDestroyView` | الـ Fragments | تسريب ذاكرة (**memory leak**): واجهة ميتة تبقى في الذاكرة |

### د) قاموس سريع: من Flutter إلى Android
كل مفهوم في هذا المشروع له مقابل تعرفه من Flutter:

| في Flutter | في هذا المشروع | القسم |
|---|---|---|
| `pubspec.yaml` | `libs.versions.toml` + `build.gradle.kts` | 1، 4 |
| Widget لشاشة كاملة | Fragment + ملف XML | 38، 39 |
| `ChangeNotifier` أو Bloc أو Cubit | `ViewModel` | 37 |
| `setState` أو `emit` | `_state.update { … }` | 37 |
| `Stream` | `Flow` / `StateFlow` | 30 |
| `StreamBuilder` أو `BlocBuilder` | `collectWhenStarted { render(it) }` | 30 |
| `Future` و`async/await` | دوال `suspend` و coroutines | 20 (د) |
| `ListView.builder` | `RecyclerView` + `Adapter` | 43 |
| `Navigator` أو `go_router` | Navigation Component + `nav_graph.xml` | 35 |
| `dio` مع Interceptors | OkHttp مع `Interceptor` | 21 |
| حزمة `retrofit` في Dart | Retrofit | 11، 12 |
| `json_serializable` | Gson + كلاسات DTO | 10 |
| `get_it` أو `injectable` أو Provider | Hilt | 23، 26، 27 |
| `flutter_secure_storage` | `EncryptedSharedPreferences` | 16 |
| `main()` و`runApp` | `FakeStoreApp` + `MainActivity` | 23، 36 |

### هـ) خمسة مصطلحات تتكرر في كل الملف
| المصطلح | المعنى باختصار |
|---|---|
| **Thread** | مسار تنفيذ مستقل. OkHttp يرسل كل طلب على thread خاص، لذلك قد تصل عدة طلبات إلى `AuthManager` في نفس اللحظة |
| **Lock** (قفل) | يضمن أن thread واحداً فقط ينفّذ جزءاً من الكود في المرة الواحدة، والبقية ينتظرون |
| **Interceptor** | كود يمر عليه كل طلب قبل إرساله، فيستطيع تعديله، مثل إضافة الـ token |
| **Dependency Injection** | الكلاس لا ينشئ ما يحتاجه بنفسه، بل يستلمه جاهزاً من الخارج (من Hilt) |
| **Blocking** | الدالة تُوقف الـ thread حتى تنتهي، مثل `.execute()` الذي ينتظر رد السيرفر |

لمصطلحات أكثر: القاموس الكامل (70+ مصطلحاً) في الملف `Android_Test_Task_Guide.pdf`.

---

## الجزء الأول: إعدادات المشروع (Gradle)

**Gradle** هو نظام البناء في Android. يقرأ هذه الملفات ليعرف:

- ما المكتبات التي يحمّلها.
- كيف يبني التطبيق.

يقابل ملف `pubspec.yaml` في Flutter.

## 1. `gradle/libs.versions.toml`
**الدور:** كتالوج الإصدارات (**Version Catalog**). كل أسماء المكتبات وإصداراتها في مكان واحد.

{{code:gradle/libs.versions.toml}}

### الشرح
الملف مقسوم إلى ثلاثة أقسام:

| القسم | ماذا يحتوي |
|---|---|
| `[versions]` | أرقام الإصدارات فقط، مثل `retrofit = "2.11.0"` |
| `[libraries]` | المكتبات. كل مكتبة لها `group` و`name`، وتشير إلى رقم إصدارها عبر `version.ref` |
| `[plugins]` | الإضافات (Plugins) التي تغيّر طريقة البناء نفسها |

**لماذا هذا الأسلوب؟** عند تحديث مكتبة تغيّر رقماً واحداً فقط. وفي ملفات Gradle تكتب `libs.retrofit` بدل النص الطويل `com.squareup.retrofit2:retrofit:2.11.0`.

**أهم المكتبات ولماذا اخترناها:**

| المكتبة | لماذا |
|---|---|
| `retrofit` + `converter-gson` | تعريف طلبات الـ API وتحويل JSON إلى كائنات Kotlin. **مطلوبة في المهمة** |
| `okhttp` + `logging-interceptor` | المحرك الذي ينفّذ الطلبات، وفيه الـ Interceptors. **مطلوبة في المهمة** |
| `security-crypto` | التخزين المشفّر للـ token |
| `hilt` | حقن الاعتماديات (Dependency Injection) |
| `navigation-fragment-ktx` / `navigation-ui-ktx` | التنقل بين الشاشات |
| `recyclerview` | القائمة. **مطلوبة في المهمة** |
| `coil` | تحميل صور المنتجات من الإنترنت |
| `mockwebserver` | سيرفر وهمي محلي للاختبارات |

## 2. `settings.gradle.kts`
**الدور:** يحدد أين يبحث Gradle عن المكتبات، وما الوحدات (**modules**) الموجودة في المشروع.

{{code:settings.gradle.kts}}

### الشرح
- `pluginManagement.repositories`: المستودعات التي تُحمَّل منها الإضافات (Plugins).
- `dependencyResolutionManagement`: المستودعات التي تُحمَّل منها المكتبات:
    - `google()`: مكتبات Android.
    - `mavenCentral()`: باقي المكتبات مثل Retrofit وOkHttp.
- `FAIL_ON_PROJECT_REPOS`: يمنع أي وحدة من إضافة مستودعات خاصة بها، فتبقى كل المصادر في مكان واحد.
- `include(":app")`: المشروع فيه وحدة واحدة اسمها `app`.

## 3. `build.gradle.kts` (الجذر)
**الدور:** يعلن الإضافات على مستوى المشروع دون أن يطبّقها.

{{code:build.gradle.kts}}

### الشرح
`apply false` تعني: "حمّل هذه الإضافة لكن لا تطبّقها هنا". كل وحدة تطبّق ما تحتاجه منها، وفي مشروعنا الوحدة الوحيدة هي `app`.

## 4. `app/build.gradle.kts`
**الدور:** ملف بناء التطبيق نفسه، وهو أهم ملفات Gradle.

{{code:app/build.gradle.kts}}

### الشرح
**الإضافات (`plugins`):**

| الإضافة | ماذا تفعل |
|---|---|
| `android.application` | تجعل الوحدة تطبيق Android قابلاً للتثبيت |
| `kotlin.android` | دعم لغة Kotlin |
| `ksp` | **Kotlin Symbol Processing**: يولّد كوداً وقت البناء، ويحتاجه Hilt |
| `hilt` | يفعّل Hilt |

**إعدادات `android { }`:**

| الإعداد | المعنى |
|---|---|
| `namespace` / `applicationId` | المعرّف الفريد للتطبيق |
| `compileSdk = 34` | نُترجم الكود مقابل Android 14 |
| `minSdk = 24` | أقل نسخة مدعومة: Android 7.0 |
| `targetSdk = 34` | النسخة التي اختُبر عليها التطبيق |
| `compileOptions` / `kotlinOptions` | استخدام Java 17 |

**الميزات (`buildFeatures`):**

- `viewBinding = true`: يولّد لكل ملف XML كلاساً يحمل مراجع عناصره، مثلاً `fragment_login.xml` ← `FragmentLoginBinding`. هذا يغني عن `findViewById` ويمنع أخطاء الأنواع والقيم الفارغة.
- `buildConfig = true`: يولّد كلاس `BuildConfig`. نستخدم منه `BuildConfig.DEBUG` لتفعيل سجل الشبكة في نسخة التطوير فقط.

**مفتاح السيرفر الوهمي (`useMockBackend`):**

- السطر `providers.gradleProperty("fakestore.useMockBackend")` يقرأ القيمة من `gradle.properties`.
- `buildConfigField(...)` يحوّلها إلى ثابت في الكود اسمه `BuildConfig.USE_MOCK_BACKEND`.
- في `debug` يأخذ قيمة المفتاح. وفي `release` يكون **دائماً `false`**، فلا يمكن أن تصل نسخة الإنتاج إلى السيرفر الوهمي بالخطأ.

**أنواع الاعتماديات (`dependencies`):**

| النوع | متى يُستخدم |
|---|---|
| `implementation` | مكتبة يحتاجها التطبيق |
| `ksp(...)` | مولّد كود يعمل وقت البناء فقط |
| `testImplementation` | مكتبة للاختبارات فقط، ولا تدخل في التطبيق النهائي |

## 5. `gradle.properties`
**الدور:** إعدادات عامة لـ Gradle.

{{code:gradle.properties}}

### الشرح
| الإعداد | المعنى |
|---|---|
| `org.gradle.jvmargs` | ذاكرة Gradle أثناء البناء |
| `android.useAndroidX` | استخدام مكتبات AndroidX الحديثة |
| `android.nonTransitiveRClass` | كل وحدة ترى موارد `R` الخاصة بها فقط، فيكون البناء أسرع |
| `fakestore.useMockBackend` | **مفتاح الوضع التجريبي.** `true` يعني أن نسخة التطوير تستخدم السيرفر الوهمي بدل FakeStore. اجعله `false` عندما يعود FakeStore للعمل. انظر الجزء التاسع |

## 6. `app/src/main/AndroidManifest.xml`
**الدور:** بطاقة هوية التطبيق: الصلاحيات، والشاشات، والإعدادات العامة.

{{code:app/src/main/AndroidManifest.xml}}

### الشرح
**الصلاحيات والإعدادات:**

- `INTERNET`: صلاحية الإنترنت. بدونها يفشل أي طلب شبكة.
- `android:name=".FakeStoreApp"`: كلاس الـ Application الذي يشغّل Hilt.
- `android:allowBackup="false"` (**قرار أمني**): يمنع نسخ ملفات التطبيق، ومنها الـ token المشفّر، إلى نسخ Google الاحتياطية. ومفتاح التشفير موجود في **Keystore** هذا الجهاز فقط، فالنسخة المستعادة على جهاز آخر لن تُفك أصلاً.
- `android:supportsRtl="true"`: دعم اللغات التي تُكتب من اليمين لليسار.

**الشاشة:**

- `MainActivity`: الشاشة الوحيدة (**Activity**).
- `exported="true"` مع `intent-filter` (MAIN + LAUNCHER): هذه الشاشة تظهر في قائمة التطبيقات وتفتح أولاً.
- `windowSoftInputMode="adjustResize"`: عند ظهور لوحة المفاتيح تتقلص الشاشة، فلا تغطي حقول تسجيل الدخول.

---

## الجزء الثاني: نماذج البيانات والـ APIs

## 7. `data/model/Product.kt`
**الدور:** شكل المنتج كما تراه الواجهة.

{{code:app/src/main/java/com/example/fakestore/data/model/Product.kt}}

### الشرح
- `data class`: كلاس لحمل البيانات. يولّد Kotlin له تلقائياً `equals` و`hashCode` و`toString` و`copy`.
- `equals` مهمة لاحقاً: يستخدمها `DiffUtil` في الـ RecyclerView ليعرف هل تغيّر المنتج.
- **كل الحقول غير قابلة لأن تكون null:** الواجهة لا تتعامل أبداً مع قيم فارغة. التنظيف يحدث في الطبقة التي تحت الواجهة (القسم 10).

## 8. `data/remote/ApiConfig.kt`
**الدور:** عنوان السيرفر الأساسي في مكان واحد.

{{code:app/src/main/java/com/example/fakestore/data/remote/ApiConfig.kt}}

### الشرح
- `object`: كائن وحيد (**Singleton**) لا يُنشأ منه نسخ.
- `const val`: ثابت معروف وقت الترجمة.
- **مهم:** العنوان ينتهي بـ `/`، وهذا شرط عند Retrofit لدمجه مع مسارات مثل `products`.

## 9. `data/remote/dto/AuthDtos.kt`
**الدور:** شكل JSON المرسل والمستقبل في تسجيل الدخول.

{{code:app/src/main/java/com/example/fakestore/data/remote/dto/AuthDtos.kt}}

### الشرح
**DTO** اختصار **Data Transfer Object**: كلاس يطابق شكل JSON حرفياً.

| الكلاس | يتحوّل إلى |
|---|---|
| `LoginRequest` | `{"username":"...","password":"..."}` |
| `LoginResponse` | يُقرأ منه `{"token":"..."}` |

**لماذا `token: String?` قابل لأن يكون null؟** مكتبة **Gson** لا تحترم قواعد Kotlin. إذا غاب الحقل من JSON تضع فيه `null` حتى لو كان النوع غير قابل لذلك، فينهار التطبيق لاحقاً في مكان غير متوقع. لذلك نعلنه `String?` ونتحقق منه بأنفسنا في `AuthManager`.

## 10. `data/remote/dto/ProductDto.kt`
**الدور:** شكل المنتج كما يأتي من السيرفر، ودالة تحويله إلى `Product`.

{{code:app/src/main/java/com/example/fakestore/data/remote/dto/ProductDto.kt}}

### الشرح
- كل الحقول قابلة لأن تكون null، لأن السيرفر خارج سيطرتنا.
- `toDomain()` دالة امتداد (**Extension Function**): تضيف دالة لكلاس موجود دون تعديله. تحوّل الـ DTO إلى `Product` نظيف:
    - `orEmpty()`: يحوّل `null` إلى نص فارغ.
    - `?:` (عامل **Elvis**): "إن كانت القيمة null فاستخدم البديل".
- اسم الحقل في JSON هو `image`، وعندنا `imageUrl`. هذا الفصل يحمي الواجهة من تغييرات السيرفر.

> **لماذا كلاسان (DTO وModel)؟** إذا غيّر السيرفر شكل الرد، نعدّل `ProductDto` و`toDomain()` فقط، والواجهة لا تتأثر.

## 11. `data/remote/AuthApi.kt`
**الدور:** تعريف طلب تسجيل الدخول.

{{code:app/src/main/java/com/example/fakestore/data/remote/AuthApi.kt}}

### الشرح
- `@POST("auth/login")`: طلب POST إلى `https://fakestoreapi.com/auth/login`.
- `@Body`: يحوّل `LoginRequest` إلى JSON ويضعه في جسم الطلب.

**لماذا ترجع `Call<LoginResponse>` وليس دالة `suspend`؟** هذا قرار مقصود:

- التجديد يحدث **داخل الـ Interceptor**.
- الـ Interceptor دالة عادية متزامنة (**blocking**) تعمل على thread خاص بـ OkHttp، وليست coroutine.
- لذلك نحتاج طلباً ننفّذه وننتظر نتيجته مباشرة بـ `call.execute()`.

**تحذير مكتوب في الكود:** هذا الـ API يُبنى على عميل OkHttp **بدون** `AuthInterceptor`. لو مرّ طلب الـ login بالـ Interceptor، فسيطلب الـ Interceptor token صالحاً، فيبدأ تجديداً، والتجديد يستدعي login من جديد… **حلقة لا نهائية**.

## 12. `data/remote/ProductApi.kt`
**الدور:** طلبات المنتجات "المحمية".

{{code:app/src/main/java/com/example/fakestore/data/remote/ProductApi.kt}}

### الشرح
- `@GET("products")`: يجلب قائمة المنتجات.
- `@GET("products/{id}")` مع `@Path("id")`: يضع رقم المنتج داخل المسار، مثل `products/5`.
- `suspend`: هنا مناسبة، لأن الـ ViewModels تستدعي هذه الدوال من coroutines.
- **لا يوجد أي ذكر للـ token هنا.** الـ header يُضاف تلقائياً بواسطة `AuthInterceptor`، لأن هذا الـ API مبني على العميل المحمي (القسم 26). هذا بالضبط معنى طلب المهمة: *"Attach Authorization: Bearer to all product requests"*.

---

## الجزء الثالث ★: منطق الـ Token (قلب المهمة)

كل ملفات هذا الجزء في المجلد `data/auth/`، وتعمل معاً هكذا:

| الملف | الدور |
|---|---|
| `Clock` | يعطي الوقت الحالي |
| `AuthSession` | ما نحفظه: الـ token، و`tokenSavedAt`، وبيانات الدخول |
| `TokenStorage` | واجهة التخزين، وتنفيذها `EncryptedTokenStorage` المشفّر |
| `AuthManager` ★ | العقل: الدخول، والانتهاء، والتجديد، والقفل، والخروج |
| `AuthInterceptor` | يضيف `Bearer` لكل طلب، ويطلب token صالحاً من `AuthManager` |
| `TokenAuthenticator` | شبكة أمان عند رد 401 |
| `AuthExceptions` / `AuthEvent` / `AuthHeaders` | أدوات مساعدة |

## 13. `data/auth/Clock.kt`
**الدور:** مصدر الوقت الحالي.

{{code:app/src/main/java/com/example/fakestore/data/auth/Clock.kt}}

### الشرح
- `fun interface`: واجهة فيها دالة واحدة فقط، تعيد الوقت بالميلي ثانية.
- `SystemTimeClock`: التنفيذ الحقيقي، يستخدم `System.currentTimeMillis()`.
- `@Inject constructor()`: يخبر Hilt أنه يستطيع إنشاء هذا الكلاس بنفسه.

**لماذا لا نستدعي `System.currentTimeMillis()` مباشرة؟** حتى نختبر الكود. في الاختبارات نستبدل الساعة بـ `FakeClock` ونقدّم الوقت 61 ثانية **فوراً**، بدل الانتظار دقيقة كاملة في كل اختبار. هذا مثال عملي على فائدة حقن الاعتماديات (**Dependency Injection**).

**لماذا `currentTimeMillis` وليس `SystemClock.elapsedRealtime`؟** لأن `tokenSavedAt` يُحفظ على القرص ويبقى بعد إعادة تشغيل الجهاز، و`elapsedRealtime` يعود إلى الصفر عند إعادة التشغيل.

## 14. `data/auth/AuthSession.kt`
**الدور:** البيانات التي نحفظها بعد تسجيل الدخول.

{{code:app/src/main/java/com/example/fakestore/data/auth/AuthSession.kt}}

### الشرح
**`Credentials`:** اسم المستخدم وكلمة المرور.

- نحتاجهما لأن **التجديد = تسجيل دخول جديد**، فـ FakeStore لا يملك endpoint للتجديد.

**`AuthSession`:**

- `token`: الرمز نفسه.
- `savedAtMillis`: هو **`tokenSavedAt`** المطلوب في المهمة.
- `credentials`: بيانات الدخول.

**تفصيل أمني مهم: `override fun toString()`**

- `data class` يطبع كل حقوله تلقائياً عند الطباعة.
- لو كتب أحد `Log.d("x", session.toString())` لظهرت كلمة المرور والـ token في Logcat.
- أعدنا تعريف `toString` لتطبع `***` بدلاً منهما.

## 15. `data/auth/TokenStorage.kt`
**الدور:** واجهة (**interface**) للتخزين: اقرأ، احفظ، امسح.

{{code:app/src/main/java/com/example/fakestore/data/auth/TokenStorage.kt}}

### الشرح
**لماذا interface؟** يوجد تنفيذان:

| التنفيذ | أين يُستخدم |
|---|---|
| `EncryptedTokenStorage` | في التطبيق: مشفّر وعلى القرص |
| `InMemoryTokenStorage` | في الاختبارات: في الذاكرة فقط |

و`AuthManager` لا يعرف ولا يهتم أيهما يستخدم.

**شرط أمان الخيوط (Thread-safety):** التطبيق يجب أن يكون آمناً عند وصول عدة threads في نفس اللحظة. OkHttp يقرأ من عدة threads، والتجديد يكتب في نفس الوقت.

## 16. `data/auth/EncryptedTokenStorage.kt`
**الدور:** تحقيق متطلب **"Securely store the token"**.

{{code:app/src/main/java/com/example/fakestore/data/auth/EncryptedTokenStorage.kt}}

### الشرح
**`MasterKey`:** المفتاح الرئيسي للتشفير.

- `AES256_GCM`: خوارزمية تشفير قوية.
- المفتاح يُنشأ ويُحفظ داخل **Android Keystore**، وهي خزنة في نظام Android لا يمكن إخراج المفاتيح منها، ولا حتى للتطبيق نفسه. التطبيق يطلب منها "شفّر" و"فك" فقط.

**`EncryptedSharedPreferences.create(...)`:** نسخة مشفّرة من SharedPreferences:

- `AES256_SIV`: يشفّر **أسماء** المفاتيح، فلا يُعرف حتى أن هناك حقلاً اسمه "token".
- `AES256_GCM`: يشفّر **القيم**.

**`by lazy`:** يُنشأ الكائن عند أول استخدام فقط، وليس عند إنشاء الكلاس، لأن إنشاءه يتعامل مع الـ Keystore وهو مكلف نسبياً.

**`read()`:**

- يقرأ الحقول الأربعة.
- إذا غاب أي منها يُرجع `null`، أي لا توجد جلسة.
- `?: return null`: إن كانت القيمة null فاخرج من الدالة فوراً.

**`save()`:**

- يكتب الحقول الأربعة دفعة واحدة.
- يستخدم `commit()`: يكتب على القرص **وينتظر** حتى ينتهي.
- البديل `apply()` يكتب في الخلفية. اخترنا `commit` لأننا دائماً على thread خلفي، فلا مشكلة في الانتظار، ونريد ضمان الحفظ.

**`clear()`:** يمسح كل شيء، ويُستخدم عند تسجيل الخروج.

**`@Synchronized` على الدوال الثلاث (أُضيف في المراجعة):**

- **المشكلة:** الجلسة مخزّنة في **أربعة** مفاتيح منفصلة، و`read()` يقرأها واحداً تلو الآخر.
- لو كتب thread التجديد جلسة جديدة **بين** قراءة الـ token وقراءة `tokenSavedAt`، لحصل القارئ على خليط: **الـ token القديم مع وقت الحفظ الجديد**. فيظن أن الـ token القديم المنتهي صالح لدقيقة كاملة.
- **الحل:** `@Synchronized` يجعل القراءة والكتابة والمسح **لا تتداخل أبداً**. كل قارئ يرى الجلسة كاملة قبل الكتابة أو بعدها، وليس في منتصفها.
- هذا قفل ثالث، منفصل عن أقفال `AuthManager`، ويُمسك لأجزاء من الثانية فقط، فلا يبطئ شيئاً.
- **في المقابلة:** هذا مثال على "عملية مركّبة" (**compound operation**). كل قراءة وحدها آمنة، لكن مجموعها ليس آمناً بلا قفل.

> **ملاحظة للمقابلة:** Google أوقفت تطوير مكتبة `security-crypto` (أصبحت **deprecated**). البديل الحديث هو **DataStore** مع تشفير بمفتاح من Keystore (مثلاً بمكتبة **Tink**). وبفضل `TokenStorage` كـ interface، يكفي كتابة كلاس جديد وتغيير سطر واحد في `DataModule`، دون لمس أي كلاس آخر.

## 17. `data/auth/AuthExceptions.kt`
**الدور:** أنواع الأخطاء الخاصة بالمصادقة.

{{code:app/src/main/java/com/example/fakestore/data/auth/AuthExceptions.kt}}

### الشرح
**`SessionExpiredException`:** لا توجد جلسة، أو السيرفر رفض التجديد. النتيجة: يجب تسجيل الدخول من جديد.

**`InvalidCredentialsException`:** السيرفر رفض اسم المستخدم أو كلمة المرور، أي رد بكود **4xx** مثل 401. نحتفظ بالكود في `httpCode`.

**`ServerUnavailableException`:** السيرفر نفسه متعطّل، أي رد بكود **5xx**. مثاله صفحات Cloudflare من نوع 52x التي تظهر عندما يتوقف FakeStore. هذا ليس خطأ المستخدم، **فلا نُخرجه**، ونعرض له رسالة "السيرفر غير متاح حالياً، حاول لاحقاً".

**⚠️ لماذا يرثان من `IOException`؟** هذه نقطة تقنية دقيقة ومهمة:

- هذه الأخطاء قد تُرمى **من داخل الـ Interceptor**.
- OkHttp يتعامل مع `IOException` كفشل طبيعي للطلب، فيمرره لـ Retrofit ثم للـ ViewModel، فيظهر للمستخدم كرسالة خطأ.
- **أي نوع آخر** (مثل `IllegalStateException`) يُعتبر خطأً برمجياً، فينهار التطبيق كاملاً (**crash**) لأنه يُرمى على thread خاص بـ OkHttp.

## 18. `data/auth/AuthEvent.kt`
**الدور:** أحداث لمرة واحدة يرسلها `AuthManager` للواجهة.

{{code:app/src/main/java/com/example/fakestore/data/auth/AuthEvent.kt}}

### الشرح
- `sealed interface`: مجموعة مغلقة من الأنواع. حالياً نوع واحد هو `SessionExpired`، ويمكن إضافة أنواع لاحقاً.
- `data object`: كائن وحيد لا يحمل بيانات.

**لماذا "حدث" (Event) وليس "حالة" (State)؟**

- تسجيل الخروج الإجباري شيء **يحدث مرة واحدة**: نعرض رسالة ثم ننتقل لشاشة Login.
- لو كان "حالة" لبقي محفوظاً، ولتكرر الانتقال عند كل تدوير للشاشة.

## 19. `data/auth/AuthHeaders.kt`
**الدور:** ثوابت ودوال لبناء header الـ Authorization وقراءته.

{{code:app/src/main/java/com/example/fakestore/data/auth/AuthHeaders.kt}}

### الشرح
- `bearer(token)`: يبني النص `Bearer abc123`.
- `tokenFrom(header)`: العكس، يستخرج `abc123` من `Bearer abc123`. يستخدمه `TokenAuthenticator` ليعرف أي token رفضه السيرفر.
- `takeIf { … }`: يُرجع القيمة إذا تحقق الشرط، وإلا يُرجع `null`.

**لماذا ملف خاص لهذه الثوابت؟** حتى لا يُكتب النص `"Authorization"` و`"Bearer "` في عدة أماكن. خطأ إملائي واحد، مثل نسيان المسافة بعد Bearer، يكسر المصادقة كلها.

## 20. ★★ `data/auth/AuthManager.kt`
**الدور:** **أهم ملف في المهمة**. المسؤول الوحيد عن حالة المصادقة.

{{code:app/src/main/java/com/example/fakestore/data/auth/AuthManager.kt}}

### الشرح التفصيلي

#### أ) الإعلان والاعتماديات (الأسطر 25–30)
- `@Singleton`: نسخة **واحدة فقط** في التطبيق كله. ضروري، لأن القفل يجب أن يكون واحداً لكل الطلبات. لو وُجدت نسختان لكان لكل منهما قفلها، ولفشل منع التجديد المكرر.
- يحصل على 3 أشياء من الخارج (حقن الاعتماديات):
    - `authApi`: لإرسال login.
    - `storage`: للحفظ.
    - `clock`: للوقت.

#### ب) الأقفال والحالة (الأسطر 33–45)
**`refreshLock`:** القفل الأساسي.

- يضمن أن **thread واحد فقط** يجدّد الـ token في المرة الواحدة.
- `Any()` كائن فارغ نستخدمه كمفتاح للقفل.

**`storageLock` و`sessionGeneration`:** لحل مشكلة دقيقة:

- **السيناريو:** بدأ تجديد، والمستخدم ضغط Logout أثناء انتظار رد السيرفر، ثم وصل الرد بـ token جديد.
- **بدون حماية:** يُحفظ الـ token الجديد بعد الخروج، **فيعود المستخدم مسجلاً للدخول رغم أنه خرج!**
- **الحل:** رقم "جيل" (`sessionGeneration`) يزيد بواحد عند كل Logout.
    1. قبل التجديد نحفظ رقم الجيل الحالي.
    2. بعد الرد نقارنه بالرقم الحالي.
    3. إذا تغيّر فهذا يعني أن خروجاً حدث، فلا نحفظ.
- `storageLock` يُمسك لجزء صغير جداً من الثانية فقط (بلا أي طلب شبكة)، فلا تنتظر الواجهة عند Logout.

**`_events` و`events`:**

- `MutableSharedFlow`: قناة بث للأحداث. خاصة وقابلة للإرسال.
- `events`: النسخة العامة، للقراءة فقط. `MainActivity` تستمع لها.
- `extraBufferCapacity = 1`: مكان لحدث واحد ينتظر، فلا يضيع إذا أُرسل والمستمع مشغول.

#### ج) `isLoggedIn()` (السطر 47)
هل توجد جلسة محفوظة؟ تستخدمها `MainActivity` لتقرر أي شاشة تبدأ بها.

#### د) `login()`: تسجيل الدخول من الشاشة (الأسطر 55–61)
- دالة `suspend`، تستدعيها `LoginViewModel` من coroutine.
- `withContext(Dispatchers.IO)`: ينقل التنفيذ إلى thread مخصص لعمليات الشبكة والقرص، لأن `requestNewSession` تنتظر الرد (blocking). لو نُفذت على الـ Main thread لتجمّد التطبيق، ورمى Android الخطأ `NetworkOnMainThreadException`.
- الخطوات: نأخذ رقم الجيل، ثم نطلب جلسة جديدة، ثم نحفظها إن لم يحدث خروج في الأثناء.

#### هـ) ★ `getValidToken()`: الدالة الأهم (الأسطر 71–81)
تُستدعى من `AuthInterceptor` قبل **كل** طلب منتجات. تمر بثلاث مراحل:

**المرحلة 1: المسار السريع (السطر 73)، بلا قفل**
```
storage.read()?.takeUnless { it.isExpired() }?.let { return it.token }
```
اقرأ الجلسة. إذا **لم** تكن منتهية فأرجع الـ token فوراً.

- `takeUnless { شرط }`: يُرجع القيمة إلا إذا تحقق الشرط.
- هذه الحالة الأكثر شيوعاً (الـ token صالح)، فلا نريد أن تنتظر الطلبات في طابور القفل بلا داعٍ.

**المرحلة 2: دخول القفل (السطر 75)**
`synchronized(refreshLock) { … }`: هنا يقف الطابور. أول thread يدخل، والبقية **ينتظرون** خارج القفل حتى يخرج.

**المرحلة 3: التحقق الثاني داخل القفل (الأسطر 77–79)، سر الحل**

- السطر 77: نقرأ الجلسة **من جديد**. إذا لم توجد (خرج المستخدم) نرمي `SessionExpiredException`.
- السطر 78: **إذا أصبح الـ token صالحاً الآن** نرجعه دون تجديد. هذا يعني أن thread آخر دخل القفل قبلنا وجدّده بينما كنا ننتظر.
- السطر 79: ما زال منتهياً، فنحن أول من وصل، **فنجدّد**.

**مثال بثلاثة طلبات A وB وC والـ token منتهٍ:**

| اللحظة | A | B | C |
|---|---|---|---|
| 1 | المسار السريع: منتهٍ | المسار السريع: منتهٍ | المسار السريع: منتهٍ |
| 2 | يدخل القفل 🔒 | ينتظر ⏳ | ينتظر ⏳ |
| 3 | التحقق الثاني: منتهٍ، فيجدّد (login) | ينتظر ⏳ | ينتظر ⏳ |
| 4 | يحفظ الجديد ويخرج 🔓 | يدخل، والتحقق الثاني: **صالح!** | ينتظر ⏳ |
| 5 | يرسل طلبه ✅ | يخرج ويرسل ✅ | يدخل، **صالح!** فيرسل ✅ |

النتيجة: **login واحد فقط**. أثبتنا ذلك في الاختبارات (القسم 51): بدون السطر 78، أنتجت 10 طلبات متزامنة **10** طلبات login.

**لماذا `synchronized` وليس `Mutex` من coroutines؟** الـ Interceptor ليس coroutine، بل دالة عادية تعمل على thread حقيقي من OkHttp، و`synchronized` هو القفل المناسب للـ threads الحقيقية. `Mutex` يحتاج دالة `suspend`، فكنا سنضطر إلى `runBlocking`، وهو أقل وضوحاً.

#### و) `refreshAfterUnauthorized()` (الأسطر 88–99)
يستدعيها `TokenAuthenticator` عندما يرد السيرفر **401**:

- السطر 90: لا توجد جلسة، فنرجع `null`، أي استسلم.
- السطر 92: إذا كان الـ token المحفوظ **يختلف** عن الذي رُفض، فطلب آخر جدّده قبلنا. نرجع الجديد دون تجديد إضافي.
- غير ذلك: نجدّد. وإذا فشل التجديد نرجع `null`.

#### ز) `logout()` و`forceLogout()` (الأسطر 102–113)
**`logout()`:** خروج عادي بطلب المستخدم.

- يزيد رقم الجيل بواحد، ثم يمسح التخزين.
- كلاهما داخل `storageLock` ليحدثا معاً كخطوة واحدة.

**`forceLogout()`:** خروج إجباري.

- يفعل نفس الشيء، ثم **يرسل حدث** `SessionExpired` للواجهة.
- `tryEmit`: يرسل دون انتظار، لأننا قد نكون على thread خاص بـ OkHttp.

#### ح) `refresh()` (الأسطر 116–127)
- تُستدعى **فقط** وهي ممسكة بـ `refreshLock`.
- تطلب جلسة جديدة بنفس بيانات الدخول المحفوظة.
- **إذا رفض السيرفر بيانات الدخول** (`InvalidCredentialsException`):
    1. تسجيل خروج إجباري (`forceLogout`).
    2. نرمي `SessionExpiredException`.
    3. **لا نعيد المحاولة**، فالمحاولة الثانية ستفشل أيضاً، وإعادتها تعني **حلقة لا نهائية**. هذا تنفيذ لتحذير المهمة: *"Avoid infinite retry loops"* و*"If refresh fails → force logout"*.
- **إذا كان الفشل بسبب الشبكة** (`IOException`): الخطأ يمر للطلب كما هو، **والجلسة تبقى**. انقطاع الإنترنت لحظياً لا يجب أن يُخرج المستخدم، والمحاولة التالية ستجدّد عندما يعود الإنترنت.

#### ط) `requestNewSession()` (الأسطر 130–142)
الطلب الفعلي إلى `/auth/login`:

- `.execute()`: ينفّذ الطلب **وينتظر** الرد (blocking).
- **السطر 135:** كود من **400 إلى 499**، أي أن السيرفر رفض البيانات، فنرمي `InvalidCredentialsException`.
- **السطر 136:** أي فشل آخر (مثل 500 أو 521، أي عطل في السيرفر) نرمي `ServerUnavailableException`. ليس ذنب المستخدم، فلا نُخرجه، وتعرض له الشاشة رسالة "السيرفر غير متاح حالياً".
- **السطر 139:** نجاح لكن بلا token (رد غريب) نرمي `IOException` أيضاً.
- **السطر 141:** ننشئ الجلسة مع **`clock.nowMillis()`**، وهذا هو **`tokenSavedAt`**.

#### ي) `isExpired()` (الأسطر 154–158)
```
age = now - savedAtMillis
expired = age < 0 || age >= 60_000
```
- `age >= 60_000`: مرت 60 ثانية أو أكثر، فالـ token **منتهٍ**. هذا متطلب *"Treat token as expired after 60 seconds"*.
- `age < 0`: الوقت الحالي **قبل** وقت الحفظ. يحدث هذا إذا غيّر المستخدم ساعة الجهاز للخلف. لا نثق بالـ token، فنعتبره منتهياً ونجدّده. حالة حدّية (**edge case**) نادرة، لكن معالجتها تُظهر الاحترافية.

#### ك) `TOKEN_LIFETIME_MS` (السطر 161)
مدة الصلاحية = `60_000L` ميلي ثانية = **60 ثانية**. الشرطة السفلية `_` لتسهيل قراءة الأرقام فقط.

## 21. `data/auth/AuthInterceptor.kt`
**الدور:** متطلب **"Authorization Interceptor"**.

{{code:app/src/main/java/com/example/fakestore/data/auth/AuthInterceptor.kt}}

### الشرح
يرث `Interceptor` من OkHttp، وفيه دالة واحدة هي `intercept(chain)`، يستدعيها OkHttp لكل طلب يمر بالعميل المحمي:

1. `authManager.getValidToken()`: اطلب token صالحاً.
   - إذا كان صالحاً يعود فوراً.
   - إذا كان منتهياً **ينتظر هنا** حتى ينتهي التجديد (واحد مشترك بين كل الطلبات).
   - النتيجة: **لا يُرسل أي طلب بـ token منتهٍ أبداً**. هذا متطلب *"Do not send requests with an expired token"*.
2. `chain.request().newBuilder()`: الطلبات في OkHttp غير قابلة للتعديل (**immutable**)، فننشئ نسخة جديدة منها.
3. `.header("Authorization", "Bearer …")`: نضيف الـ header. `header` تستبدل أي قيمة سابقة بنفس الاسم، بخلاف `addHeader` التي تضيف نسخة ثانية.
4. `chain.proceed(request)`: نمرر الطلب للحلقة التالية في السلسلة (Interceptor آخر أو الشبكة)، ونرجع الرد.

> **تشبيه:** موظف عند باب الشركة يختم كل رسالة خارجة. إذا كان الختم منتهياً يطلب ختماً جديداً أولاً، وإذا جاء معه زملاء في نفس اللحظة ينتظرون الختم الجديد ولا يطلب كل منهم ختماً.

## 22. `data/auth/TokenAuthenticator.kt`
**الدور:** شبكة أمان إضافية عند رد **401 Unauthorized**.

{{code:app/src/main/java/com/example/fakestore/data/auth/TokenAuthenticator.kt}}

### الشرح
**ما هو Authenticator في OkHttp؟** كلاس يستدعيه OkHttp **تلقائياً** عندما يرد السيرفر بالكود 401، أي "غير مصادَق".

- إذا أرجع طلباً جديداً، يعيد OkHttp المحاولة به.
- إذا أرجع `null`، يتوقف ويُرجع رد الـ 401 كما هو.

**لماذا نحتاجه إذا كان FakeStore لا يرد بـ 401 أبداً؟** في السيرفرات الحقيقية قد يرفض السيرفر token نعتبره صالحاً، مثلاً إذا ألغاه المسؤول أو اختلفت الساعات. هذا الكلاس يُظهر للمراجع أنك تعرف كيف تُبنى المصادقة في التطبيقات الحقيقية.

| | الأسلوب | متى يعمل |
|---|---|---|
| `AuthInterceptor` | **استباقي (Proactive)** | قبل الإرسال |
| `TokenAuthenticator` | **تفاعلي (Reactive)** | بعد رد 401 |

**المنطق خطوة بخطوة:**

1. `response.priorResponse != null`: هذا الطلب **أُعيدت محاولته مرة من قبل** ورُفض مرة ثانية. نسجّل خروجاً إجبارياً ونرجع `null`.
   هكذا **لا تتكرر المحاولة أكثر من مرة**، **فلا توجد حلقة لا نهائية**.

2. نستخرج الـ token المرفوض من الطلب.
3. نطلب من `AuthManager` تجديداً، وهو يتحقق أولاً هل جدّد طلب آخر قبلنا.
4. نعيد بناء الطلب بالـ token الجديد، فيرسله OkHttp مرة ثانية.

---

## الجزء الرابع: الـ Repository وحقن الاعتماديات (Hilt)

**Dependency Injection (حقن الاعتماديات):** بدل أن ينشئ كل كلاس ما يحتاجه بنفسه، **يُعطى** له من الخارج.

**تشبيه:** الطاهي لا يزرع الخضار بنفسه، بل يستلمها جاهزة من المورّد. **Hilt** هو "المورّد": يعرف كيف يصنع كل شيء، ويوصله لمن يحتاجه.

## 23. `FakeStoreApp.kt`
**الدور:** كلاس التطبيق، وأول ما يُنشأ عند التشغيل.

{{code:app/src/main/java/com/example/fakestore/FakeStoreApp.kt}}

### الشرح
- `@HiltAndroidApp`: يجعل Hilt يولّد "الحاوية" الرئيسية للاعتماديات، التي تعيش طوال عمر التطبيق.
- بدونه لا يعمل أي `@Inject` أو `@AndroidEntryPoint` في المشروع.
- مسجّل في الـ Manifest عبر `android:name`.

## 24. `data/repository/ProductRepository.kt`
**الدور:** المصدر الوحيد لبيانات المنتجات بالنسبة للـ ViewModels.

{{code:app/src/main/java/com/example/fakestore/data/repository/ProductRepository.kt}}

### الشرح
- **interface** للفصل: الـ ViewModel يعرف `ProductRepository` فقط، ولا يعرف Retrofit.
- `ProductRepositoryImpl`: يستدعي الـ API ويحوّل كل DTO إلى `Product` عبر `toDomain()`.
- `map { it.toDomain() }`: يحوّل كل عنصر في القائمة.
- `@Inject constructor(private val api: ProductApi)`: Hilt يعطيه `ProductApi` جاهزاً، ومعه العميل المحمي والـ Interceptor.

**هذه طبقة الـ Model في MVVM.** لو أضفنا لاحقاً تخزيناً محلياً (Room) للعمل بلا إنترنت، نعدّل هذا الكلاس فقط.

## 25. `di/Qualifiers.kt`
**الدور:** تسميتان لتمييز عميلَي OkHttp.

{{code:app/src/main/java/com/example/fakestore/di/Qualifiers.kt}}

### الشرح
**المشكلة:** عندنا كائنان من **نفس النوع** `OkHttpClient`، وHilt يميّز الأشياء بنوعها، فلن يعرف أيهما يعطي.

**الحل: `@Qualifier`**، أي annotation نصنعها لتكون "ملصقاً" يميّز بينهما:

| الملصق | العميل |
|---|---|
| `@PlainClient` | بلا مصادقة، **لطلب login فقط** |
| `@AuthClient` | مع `AuthInterceptor` و`TokenAuthenticator`، للمنتجات |

`@Retention(BINARY)`: الملصق محفوظ في الكود المترجَم، وهذا ما يحتاجه Hilt.

## 26. ★ `di/NetworkModule.kt`
**الدور:** "وصفة" بناء كل ما يخص الشبكة، وهنا يتحقق **فصل العميلين**.

{{code:app/src/main/java/com/example/fakestore/di/NetworkModule.kt}}

### الشرح
**مصطلحات Hilt في هذا الملف:**

| المصطلح | المعنى |
|---|---|
| `@Module` + `@InstallIn(SingletonComponent::class)` | وحدة وصفات تعيش طوال عمر التطبيق |
| `@Provides` | "هكذا تصنع هذا الشيء". نحتاجه لكلاسات من مكتبات خارجية لا نستطيع وضع `@Inject` عليها |
| `@Singleton` | اصنعه مرة واحدة فقط وأعد استخدامه |

**`provideLoggingInterceptor`:** سجل الطلبات في Logcat.

- مستوى `BASIC`: سطر واحد لكل طلب (الطريقة، والعنوان، والنتيجة). هذا كافٍ لإثبات أن login حدث **مرة واحدة**.
- يعمل في نسخة التطوير (`DEBUG`) فقط، ومعطّل في النسخة النهائية.
- `redactHeader("Authorization")`: يخفي الـ token من السجل.
- **لا نسجّل جسم الطلبات أبداً**، لأن جسم طلب login يحتوي **كلمة المرور**.

**`providePlainClient`:** العميل العادي. فيه سجل الطلبات فقط، **بلا** `AuthInterceptor`.

**`provideAuthClient`:** العميل المحمي:

- **الترتيب مهم:** `authInterceptor` أولاً ليضيف الـ token، ثم `logging` ليسجّل الطلب النهائي.
- `.authenticator(tokenAuthenticator)`: لمعالجة رد 401.

**`provideAuthApi` و`provideProductApi`:** كل API مع عميله:

- `AuthApi` ← `@PlainClient`.
- `ProductApi` ← `@AuthClient`.

**الدوال المساعدة:**

- `baseClientBuilder`: الإعدادات المشتركة، أي مهلة 20 ثانية (**timeout**) للاتصال والقراءة.
- `buildRetrofit`: ينشئ Retrofit بالعنوان الأساسي ومحوّل Gson.

**`mockBackend` و`addMockBackend` (للتطوير فقط):**

- إذا كان `BuildConfig.USE_MOCK_BACKEND` يساوي `true`، يُنشأ `MockBackendInterceptor` واحد يشترك فيه العميلان.
- `addMockBackend` يضيفه **في آخر السلسلة**، بعد `AuthInterceptor` وسجل الطلبات، فكل شيء قبله يعمل كما في الإنتاج.
- إذا كان `false` يكون `null`، ولا يُضاف شيء، فتذهب الطلبات إلى FakeStore الحقيقي.
- لم نضعه في Hilt عن قصد، لأنه مفتاح وقت البناء، وليس اعتمادية يحتاجها كلاس آخر.

> **خلاصة هذا الملف:** هنا تحديداً نمنع **الحلقة اللانهائية**. طلب login لا يمر أبداً بالـ `AuthInterceptor`.

## 27. `di/DataModule.kt`
**الدور:** ربط كل interface بتنفيذه الحقيقي.

{{code:app/src/main/java/com/example/fakestore/di/DataModule.kt}}

### الشرح
`@Binds` معناها: "عندما يطلب أحد X أعطه Y". وهي أخف من `@Provides` لأنها لا تحتاج كتابة كود إنشاء.

| يُطلب | يُعطى |
|---|---|
| `TokenStorage` | `EncryptedTokenStorage` |
| `Clock` | `SystemTimeClock` |
| `ProductRepository` | `ProductRepositoryImpl` |

**لتغيير طريقة التخزين لاحقاً** (مثلاً إلى DataStore): غيّر سطراً واحداً هنا.

---

## الجزء الخامس: أدوات الواجهة المشتركة

## 28. `ui/common/UiState.kt`
**الدور:** حالات أي شاشة تحمّل بيانات.

{{code:app/src/main/java/com/example/fakestore/ui/common/UiState.kt}}

### الشرح
- `sealed interface` بثلاث حالات فقط، والشاشة في **واحدة منها دائماً**:

| الحالة | ماذا تعرض الشاشة |
|---|---|
| `Loading` | دائرة تحميل |
| `Success(data)` | البيانات |
| `Error(messageRes)` | رسالة خطأ |

- `<out T>`: يسمح باستخدام `Loading` (النوع `Nothing`) مكان أي `UiState<أي شيء>`.
- `@StringRes Int`: رقم مورد نصي مثل `R.string.error_network`، وليس نصاً مكتوباً. هكذا تبقى النصوص في `strings.xml`، فيسهل ترجمتها لاحقاً.

## 29. `ui/common/ErrorMessages.kt`
**الدور:** تحويل أي خطأ تقني إلى رسالة مفهومة للمستخدم.

{{code:app/src/main/java/com/example/fakestore/ui/common/ErrorMessages.kt}}

### الشرح
**الترتيب مهم: من الأكثر تحديداً إلى الأعم.** `InvalidCredentialsException` و`SessionExpiredException` و`ServerUnavailableException` كلها ترث من `IOException`. لو وضعنا `is IOException` أولاً، لظهرت لهما رسالة "لا يوجد إنترنت" الخاطئة.

| الخطأ | الرسالة |
|---|---|
| `InvalidCredentialsException` | اسم المستخدم أو كلمة المرور خاطئة |
| `SessionExpiredException` | انتهت الجلسة |
| `ServerUnavailableException` | السيرفر غير متاح حالياً، حاول لاحقاً |
| `HttpException` بكود **5xx** (من Retrofit) | السيرفر غير متاح حالياً، حاول لاحقاً |
| `HttpException` بكود آخر | خطأ في السيرفر |
| `IOException` | لا يوجد إنترنت |
| أي شيء آخر | حدث خطأ ما |

## 30. `ui/common/FlowExt.kt`
**الدور:** دالة مساعدة لمراقبة الـ Flow بأمان داخل Fragment.

{{code:app/src/main/java/com/example/fakestore/ui/common/FlowExt.kt}}

### الشرح
**المشكلة:** الـ Fragment له دورة حياة، وواجهته (**View**) تُدمَّر وتُنشأ من جديد، مثلاً عند الانتقال لشاشة أخرى ثم العودة. لو بقينا نحدّث واجهة مدمّرة لحدث crash أو تسريب ذاكرة.

**الحل:**

- `viewLifecycleOwner`: دورة حياة **الواجهة** فقط، وليس الـ Fragment كله.
- `repeatOnLifecycle(STARTED)`:
    - يبدأ المراقبة عندما تصبح الشاشة ظاهرة.
    - يوقفها عندما تختفي، مثلاً عند الذهاب للخلفية.
    - يعيد تشغيلها عند العودة.
- عند تدمير الواجهة يتوقف كل شيء تلقائياً.

**الاستخدام:** في كل Fragment نكتب سطراً واحداً:
```
collectWhenStarted(viewModel.state) { render(it) }
```

## 31. `ui/common/Formatters.kt`
**الدور:** تنسيق السعر.

{{code:app/src/main/java/com/example/fakestore/ui/common/Formatters.kt}}

### الشرح
- `NumberFormat.getCurrencyInstance(Locale.US)`: يحوّل `109.95` إلى `$109.95`.
- `Locale.US` ثابت لأن أسعار FakeStore بالدولار، مهما كانت لغة الجهاز.

---

## الجزء السادس: الشاشات (View + ViewModel)

**نمط MVVM في كل شاشة:**

| الطبقة | الملفات | الدور |
|---|---|---|
| **View** | Fragment + ملف XML | تعرض الحالة، وتُبلغ الـ ViewModel بضغطات المستخدم |
| **ViewModel** | `…ViewModel.kt` | يطلب البيانات، ويحوّلها إلى `UiState`، ويبقى حياً عند تدوير الشاشة |
| **Model** | Repository / `AuthManager` | البيانات |

## 32. `res/values/strings.xml`
**الدور:** كل نصوص التطبيق.

{{code:app/src/main/res/values/strings.xml}}

### الشرح
- لا نكتب نصوصاً داخل الكود أو XML مباشرة، فكل نص هنا. هذا يسهّل الترجمة لاحقاً بملف `values-ar/strings.xml`.
- `%1$d` و`%1$.1f`: أماكن لقيم تُملأ وقت التشغيل:
    - `%1$d`: رقم صحيح (الأول).
    - `%1$.1f`: رقم عشري بخانة واحدة بعد الفاصلة.
- `test_account_hint`: يعرض بيانات حساب الاختبار على شاشة الدخول، لتسهيل التجربة على المراجع.

## 33. `res/values/themes.xml`
**الدور:** ثيم التطبيق.

{{code:app/src/main/res/values/themes.xml}}

### الشرح
- `Theme.Material3.DayNight`: تصميم Material 3 مع دعم الوضع الليلي تلقائياً.
- `NoActionBar`: بلا شريط علوي افتراضي، لأننا نضع `MaterialToolbar` بأنفسنا في `activity_main.xml`.

## 34. `res/layout/activity_main.xml`
**الدور:** هيكل الشاشة الرئيسية: شريط علوي، ومساحة تتبدل فيها الشاشات.

{{code:app/src/main/res/layout/activity_main.xml}}

### الشرح
- `MaterialToolbar` داخل `AppBarLayout`: الشريط العلوي. يعرض عنوان الشاشة الحالية، وسهم الرجوع، والقائمة (⋮).
- `FragmentContainerView` مع `NavHostFragment`: **المساحة التي تُعرض فيها الشاشات** (Fragments)، ومكوّن Navigation يبدّلها.
- `app:defaultNavHost="true"`: زر الرجوع في الجهاز يتحكم بالتنقل داخل هذه المساحة.
- `layout_height="0dp"` مع `layout_weight="1"`: تأخذ كل المساحة المتبقية تحت الشريط.
- **لا يوجد `app:navGraph` هنا عن قصد.** نحدد خريطة التنقل من الكود في `MainActivity`، لنختار شاشة البداية.

## 35. `res/navigation/nav_graph.xml`
**الدور:** خريطة التنقل: ما الشاشات، وكيف ننتقل بينها.

{{code:app/src/main/res/navigation/nav_graph.xml}}

### الشرح
- `<fragment>`: كل شاشة لها `id` واسم كلاسها (`android:name`) وعنوانها (`label`)، والعنوان يظهر تلقائياً في الشريط العلوي.
- `<action>`: انتقال مسموح من شاشة إلى أخرى.

**`action_login_to_products`:**

- `popUpTo="@id/loginFragment"` + `popUpToInclusive="true"`: عند الانتقال للمنتجات **تُحذف شاشة Login من سجل الرجوع** (**back stack**).
- لولا ذلك، لعاد المستخدم لشاشة Login بزر الرجوع وهو مسجّل الدخول.

**`<argument android:name="productId">`:**

- شاشة التفاصيل تحتاج رقم المنتج.
- `argType="integer"`: نوعه رقم صحيح.

**`action_global_login`:** انتقال عام (**global action**)، يمكن استخدامه من أي شاشة:

- `popUpTo="@id/nav_graph"` + `inclusive`: **يمسح كل الشاشات** ثم يفتح Login.
- يُستخدم في Logout، وفي الخروج الإجباري. هذا تحقيق لمتطلب *"Redirect user to Login screen"*. بعده، زر الرجوع يُغلق التطبيق بدل العودة للمنتجات.

## 36. `MainActivity.kt`
**الدور:** الـ Activity الوحيدة. تختار شاشة البداية، وتستمع للخروج الإجباري.

{{code:app/src/main/java/com/example/fakestore/MainActivity.kt}}

### الشرح
**`@AndroidEntryPoint`:** يسمح لـ Hilt بحقن الاعتماديات هنا وفي الـ Fragments داخلها.

**`@Inject lateinit var authManager`:** حقن في حقل (**field injection**). Hilt يملؤه قبل `onCreate`. نستخدم هذا الأسلوب لأن Android هو من ينشئ الـ Activity، وليس نحن، فلا نستطيع تمرير شيء لـ constructor.

**`onCreate`:**

1. `ActivityMainBinding.inflate`: ننشئ الواجهة عبر ViewBinding.
2. `setSupportActionBar(binding.toolbar)`: نجعل الـ Toolbar هو الشريط العلوي.
3. نحصل على `navController` من `NavHostFragment`.
4. **اختيار شاشة البداية:**
   - نحمّل خريطة التنقل من XML.
   - نحدد البداية: إذا كانت هناك جلسة محفوظة نبدأ بـ **المنتجات**، وإلا بـ **Login**.
   - حتى لو كان الـ token منتهياً، سيُجدَّد تلقائياً مع أول طلب.
5. `AppBarConfiguration(setOf(login, products))`: هاتان شاشتان رئيسيتان (**top-level**)، فلا يظهر عليهما سهم الرجوع. أما التفاصيل فيظهر عليها.

**`observeSession()`:** مراقبة الجلسة، تعمل فقط والتطبيق ظاهر.

1. **أولاً:** إذا لم تكن هناك جلسة نذهب لشاشة Login. قد تكون الجلسة مُسحت والتطبيق في الخلفية، حين لم يكن أحد يستمع للحدث.
2. **ثانياً:** نستمع لـ `authManager.events`. عند وصول `SessionExpired` ننتقل لـ Login مع رسالة.

**لماذا هنا في MainActivity وليس في كل Fragment؟** الخروج الإجباري قد يحدث **في أي شاشة**، أثناء القائمة أو التفاصيل. الـ Activity موجودة دائماً، فهي المكان المناسب لاستقبال الحدث مرة واحدة.

**`goToLogin`:** إذا كنا أصلاً في Login لا نفعل شيئاً، فهذا يمنع فتح Login مرتين.

## 37. `ui/login/LoginViewModel.kt`
**الدور:** منطق شاشة تسجيل الدخول.

{{code:app/src/main/java/com/example/fakestore/ui/login/LoginViewModel.kt}}

### الشرح
**`LoginUiState`:** كل ما تحتاجه الشاشة في كائن واحد:

| الحقل | المعنى |
|---|---|
| `isLoading` | يجري تسجيل الدخول؟ (دائرة تحميل، وزر معطّل) |
| `errorRes` | رسالة خطأ، أو `null` إن لم يكن هناك خطأ |
| `isLoggedIn` | نجح الدخول؟ (إشارة للانتقال) |

**`MutableStateFlow` / `StateFlow`:**

- `_state` خاص وقابل للتعديل.
- `state` عام وللقراءة فقط: **الشاشة تقرأ ولا تعدّل**.
- `_state.update { it.copy(...) }`: ينسخ الحالة الحالية مع تغيير حقول معينة، بشكل آمن مع الـ threads.

**`login()` خطوة بخطوة:**

1. إذا كان التحميل جارياً نتجاهل الضغطة، فهذا يمنع **الضغط المزدوج** على الزر.
2. التحقق المحلي: إذا كانت الحقول فارغة نعرض خطأ **دون** إرسال طلب.
3. `viewModelScope.launch`: نبدأ coroutine تُلغى تلقائياً إذا أُغلقت الشاشة نهائياً.
4. نجعل الحالة Loading.
5. نستدعي `authManager.login()`:
   - **نجاح:** `isLoggedIn = true`.
   - **فشل:** نحوّل الخطأ إلى رسالة عبر `toMessageRes()`.

**`catch (e: CancellationException) { throw e }`:**

- إلغاء الـ coroutine ليس خطأً.
- إذا التقطناه ضمن `catch (e: Exception)` ولم نمرره، تتعطل آلية الإلغاء.
- لذلك نعيد رميه دائماً، وهذه ممارسة احترافية مهمة.

## 38. `res/layout/fragment_login.xml`
**الدور:** تصميم شاشة تسجيل الدخول.

{{code:app/src/main/res/layout/fragment_login.xml}}

### الشرح
- `ScrollView` مع `fillViewport`: الشاشة قابلة للتمرير إذا غطّتها لوحة المفاتيح.
- `TextInputLayout` + `TextInputEditText`: حقل إدخال من Material، فيه عنوان عائم وإطار.
- `inputType="textPassword"`: يُخفي كلمة المرور بنقاط.
- `endIconMode="password_toggle"`: أيقونة عين لإظهار كلمة المرور أو إخفائها.
- `imeOptions`: زر لوحة المفاتيح:
    - `actionNext` في حقل الاسم: ينتقل لحقل كلمة المرور.
    - `actionDone` في حقل كلمة المرور: ينفّذ الدخول.
- `autofillHints`: يسمح لمدير كلمات المرور بملء الحقول.
- `inputType="textVisiblePassword|textNoSuggestions"` في **حقل اسم المستخدم** (أُضيف في المراجعة):
    - **المشكلة التي واجهناها:** لوحة مفاتيح Infinix كبّرت أول حرف، فأصبح `mor_2314` هو `Mor_2314`، ورفض السيرفر الدخول.
    - `textVisiblePassword`: يجعل لوحة المفاتيح تعامل الحقل كنص حرفي، **فلا تكبّر ولا تصحّح**.
    - `textNoSuggestions`: يلغي شريط الاقتراحات.
    - النتيجة: ما تكتبه يصل للسيرفر **حرفاً بحرف**، وهذا مهم لأن أسماء المستخدمين حساسة لحالة الأحرف.
- `error_text`: رسالة الخطأ، مخفية (`gone`) حتى يحدث خطأ. `?attr/colorError` يجعل لونها أحمر من الثيم.
- `CircularProgressIndicator`: دائرة التحميل، مخفية في البداية.
- `mock_backend_banner`: شريط ملوّن في أعلى الشاشة يقول إن التطبيق في **الوضع التجريبي**. مخفي إلا إذا كان السيرفر الوهمي مفعّلاً، حتى لا يظن أحد أن البيانات حقيقية.

## 39. `ui/login/LoginFragment.kt`
**الدور:** شاشة تسجيل الدخول (الـ View).

{{code:app/src/main/java/com/example/fakestore/ui/login/LoginFragment.kt}}

### الشرح
- `Fragment(R.layout.fragment_login)`: الـ Fragment ينشئ واجهته من هذا الملف تلقائياً.
- `by viewModels()`: يحصل على الـ ViewModel، وHilt يوفّر له `AuthManager`. الـ ViewModel **نفسه** يعود بعد تدوير الشاشة.

**نمط `_binding` / `binding`:**

- واجهة الـ Fragment تُدمَّر قبل الـ Fragment نفسه.
- لذلك نجعل `_binding = null` في `onDestroyView`، حتى لا نحتفظ بواجهة ميتة في الذاكرة (**memory leak**).
- `binding` (مع `!!`) للاستخدام المريح بينهما.

**`onViewCreated`:**

- `mockBackendBanner.isVisible = BuildConfig.USE_MOCK_BACKEND`: يُظهر شريط الوضع التجريبي فقط عند تفعيله.
- ربط الزر وزر Done في لوحة المفاتيح بالدالة `submit()`.
- `collectWhenStarted(viewModel.state) { render(it) }`: كلما تغيّرت الحالة تُرسم الشاشة من جديد.

**`render(state)`:** الشاشة **تعكس** الحالة فقط:

- التحميل: تظهر الدائرة، ويُعطّل الزر والحقول.
- الخطأ: تظهر الرسالة.
- `isLoggedIn`: تنتقل للمنتجات.
    - الشرط `currentDestination == loginFragment` يمنع تكرار الانتقال، وهو ما كان سيسبب crash.

## 40. `res/layout/view_error.xml`
**الدور:** واجهة خطأ قابلة لإعادة الاستخدام: رسالة + زر إعادة المحاولة.

{{code:app/src/main/res/layout/view_error.xml}}

### الشرح
- تُضمَّن في شاشتي المنتجات والتفاصيل بالوسم `<include>`، بدل تكرار نفس التصميم.
- ViewBinding يولّد لها `ViewErrorBinding`، فنصل لعناصرها مثلاً عبر `binding.errorView.retryButton`.

## 41. `ui/products/ProductsViewModel.kt`
**الدور:** منطق شاشة المنتجات.

{{code:app/src/main/java/com/example/fakestore/ui/products/ProductsViewModel.kt}}

### الشرح
**`ProductsEvent`:** أحداث لمرة واحدة:

- `LoggedOut`: الانتقال لـ Login.
- `ConcurrencyTestFinished`: عرض رسالة نجاح اختبار التزامن.
- `ShowError`: عرض رسالة خطأ.

**الحالة مقابل الأحداث:**

| | النوع | السلوك |
|---|---|---|
| `state` | `StateFlow` | يحتفظ بآخر قيمة. القائمة تبقى بعد تدوير الشاشة |
| `events` | `Channel` | كل حدث يُستهلك **مرة واحدة**. رسالة الخروج لا تتكرر عند التدوير |

**`init { loadProducts() }`:** يبدأ التحميل فور إنشاء الـ ViewModel، مرة واحدة فقط وليس عند كل تدوير.

**`loadProducts()`:** Loading، ثم Success أو Error.

- الطلب يمر **تلقائياً** بالـ `AuthInterceptor`، فلا يوجد أي ذكر للـ token هنا.
- **هذا هدف التصميم كله:** الـ ViewModel لا يعرف شيئاً عن الـ tokens.

**★ `runConcurrencyTest()`: أداة لإثبات متطلب التزامن للمراجع**

- `coroutineScope { … }`: ينتظر انتهاء كل الطلبات بداخله.
- `async { repository.getProduct(id) }`: يطلق طلباً **دون انتظار** انتهائه، فتنطلق الطلبات الخمسة **في نفس اللحظة**.
- `awaitAll()`: ينتظر الخمسة معاً.
- **طريقة العرض:** سجّل الدخول، وانتظر أكثر من 60 ثانية، ثم اضغط "Run concurrency test". في Logcat سيظهر **طلب login واحد فقط** ثم 5 طلبات منتجات.

**`logout()`:** يمسح الجلسة، ثم يرسل حدث `LoggedOut` لتنتقل الشاشة إلى Login.

## 42. `res/layout/item_product.xml`
**الدور:** تصميم **صف واحد** في القائمة.

{{code:app/src/main/res/layout/item_product.xml}}

### الشرح
- `MaterialCardView`: بطاقة لها ظل وزوايا دائرية، وقابلة للضغط (`clickable`).
- **الصف أفقي:** صورة 72dp، ثم عمود نصوص (العنوان، والتصنيف، والسعر).
- `layout_width="0dp"` + `layout_weight="1"`: عمود النصوص يأخذ كل المساحة المتبقية بجانب الصورة.
- `maxLines="2"` + `ellipsize="end"`: العنوان الطويل يُقص بـ "…".

## 43. ★ `ui/products/ProductAdapter.kt`
**الدور:** يربط قائمة المنتجات بالـ RecyclerView. هذا متطلب **"RecyclerView"**.

{{code:app/src/main/java/com/example/fakestore/ui/products/ProductAdapter.kt}}

### الشرح
**كيف يعمل RecyclerView؟** القائمة فيها 20 منتجاً والشاشة تعرض 7 فقط. RecyclerView ينشئ **نحو 9 صفوف** فقط. عند التمرير، الصف الذي يخرج من الأعلى **يُعاد استخدامه** (**recycled**) لعرض منتج جديد في الأسفل. هذا يوفّر الذاكرة ويجعل التمرير سلساً.

**`onCreateViewHolder`:**

- يُنشئ صفاً جديداً من `item_product.xml`.
- يُستدعى **قليلاً**، بعدد الصفوف الظاهرة تقريباً.

**`onBindViewHolder`:**

- يضع بيانات منتج على صف موجود.
- يُستدعى **كثيراً**، مع كل تمرير.

**`ProductViewHolder`:** يمسك عناصر الصف (عبر binding) لإعادة استخدامها.

- **مستمع الضغط يُضبط مرة واحدة** في `init`، وليس في كل `bind`، فهذا أكفأ.
- يقرأ المنتج الحالي من المتغير `product`.
- `binding.image.load(url)`: مكتبة **Coil** تحمّل الصورة من الإنترنت في الخلفية، وتخزّنها مؤقتاً (**cache**)، وتعرضها بتأثير تلاشٍ (`crossfade`).

**`ListAdapter` + `DiffUtil`:**

- عند وصول قائمة جديدة عبر `submitList`، يقارن `DiffUtil` القديمة بالجديدة **في الخلفية**، ويحدّث فقط الصفوف التي تغيّرت، مع حركة.
- `areItemsTheSame`: نفس المنتج؟ يقارن الـ `id`.
- `areContentsTheSame`: هل تغيّرت بياناته؟ يقارن كل الحقول عبر `equals` الخاصة بـ `data class`.

## 44. `res/layout/fragment_products.xml`
**الدور:** تصميم شاشة القائمة.

{{code:app/src/main/res/layout/fragment_products.xml}}

### الشرح
- `FrameLayout`: العناصر فوق بعضها، ونُظهر واحداً حسب الحالة: القائمة، أو التحميل، أو الخطأ.
- `app:layoutManager="LinearLayoutManager"`: ترتيب الصفوف عمودياً.
- `clipToPadding="false"`: الصفوف تُرسم داخل الهامش أثناء التمرير، وهذا يعطي شكلاً أجمل.

## 45. `res/menu/menu_products.xml`
**الدور:** القائمة العلوية (⋮) في شاشة المنتجات.

{{code:app/src/main/res/menu/menu_products.xml}}

### الشرح
ثلاثة خيارات داخل القائمة المنسدلة (`showAsAction="never"`):

- **Refresh:** إعادة تحميل المنتجات.
- **Run concurrency test:** اختبار التزامن.
- **Log out:** تسجيل الخروج.

## 46. `ui/products/ProductsFragment.kt`
**الدور:** شاشة قائمة المنتجات.

{{code:app/src/main/java/com/example/fakestore/ui/products/ProductsFragment.kt}}

### الشرح
**`adapter`:** يُنشأ مرة واحدة. عند الضغط على منتج نفتح التفاصيل مع رقمه:
```
bundleOf("productId" to product.id)
```

**`render(state)`:**

- `isVisible`: نُظهر عنصراً واحداً فقط حسب الحالة.
- `Success`: `adapter.submitList(...)`.
- `Error`: نضع نص الخطأ.

**`handle(event)`:**

- `LoggedOut`: الانتقال لـ Login مع مسح كل الشاشات.
- `ConcurrencyTestFinished` / `ShowError`: رسالة **Snackbar** أسفل الشاشة.

**`setUpMenu()`:**

- `MenuProvider` هو الطريقة الحديثة لإضافة قائمة من Fragment.
- `viewLifecycleOwner, RESUMED`: القائمة تظهر فقط والشاشة نشطة، وتُزال تلقائياً عند مغادرتها.

**`onDestroyView`:**

- `productsList.adapter = null`: الـ adapter يعيش أطول من الواجهة، فنفصله عنها لتجنّب تسريب الذاكرة.
- ثم `_binding = null`.

## 47. `ui/detail/ProductDetailViewModel.kt`
**الدور:** منطق شاشة التفاصيل.

{{code:app/src/main/java/com/example/fakestore/ui/detail/ProductDetailViewModel.kt}}

### الشرح
**`SavedStateHandle`:** يحمل الـ arguments القادمة من Navigation، أي `productId`.

- **ميزته:** يبقى حتى لو أغلق النظام التطبيق في الخلفية لتوفير الذاكرة (**process death**) ثم أعاده المستخدم.
- `checkNotNull(...)`: إذا لم يصل الرقم، فهذا خطأ برمجي، فنفشل فوراً برسالة واضحة.

**`ARG_PRODUCT_ID = "productId"`:**

- يجب أن يطابق اسم الـ argument في `nav_graph.xml`.
- ثابت واحد يستخدمه الطرفان، فلا يحدث خطأ إملائي.

**`loadProduct()`:** نفس نمط القائمة. وطلب المنتج أيضاً يمر بالـ Interceptor تلقائياً.

## 48. `res/layout/fragment_product_detail.xml`
**الدور:** تصميم شاشة التفاصيل.

{{code:app/src/main/res/layout/fragment_product_detail.xml}}

### الشرح
- `NestedScrollView` (معرّفه `content`): المحتوى قابل للتمرير، لأن الوصف قد يكون طويلاً. مخفي حتى تصل البيانات.
- المحتوى بالترتيب: صورة كبيرة 260dp، ثم العنوان، والسعر (بلون الثيم الأساسي)، والتصنيف، والتقييم، والوصف.
- `lineSpacingMultiplier="1.2"`: مسافة أكبر بين الأسطر لتسهيل قراءة الوصف.

## 49. `ui/detail/ProductDetailFragment.kt`
**الدور:** شاشة التفاصيل.

{{code:app/src/main/java/com/example/fakestore/ui/detail/ProductDetailFragment.kt}}

### الشرح
- نفس نمط شاشة القائمة: binding، ومراقبة الحالة، و`render`.
- `showProduct()`: يملأ العناصر. التقييم يُبنى من مورد نصي بقيمتين:
```
getString(R.string.product_rating, rating, count)  →  "★ 4.1 (259 reviews)"
```
- سهم الرجوع في الشريط العلوي يعمل تلقائياً، لأن التفاصيل ليست شاشة رئيسية في `AppBarConfiguration`.

---

## الجزء السابع: الاختبارات (Unit Tests)

**مكان الاختبارات:** المجلد `app/src/test/`. تعمل على الكمبيوتر مباشرة (**JVM**)، بلا هاتف أو محاكي.
**طريقة التشغيل:** الأمر `./gradlew test`، أو بزر ▶️ بجانب أي اختبار في Android Studio.

**النتيجة:** **21 اختباراً، كلها ناجحة**.

- شُغّلت 5 مرات متتالية للتأكد من أن اختبارات التزامن ثابتة وغير عشوائية.
- **وأُثبت أنها تكشف الخطأ فعلاً:** عند حذف التحقق الثاني داخل القفل، فشل اختباران:
    - 10 طلبات متزامنة أنتجت **10** طلبات login بدل 1.
    - 5 طلبات Retrofit متوازية أنتجت **5** طلبات login بدل 1.

## 50. `test/.../TestDoubles.kt`
**الدور:** بدائل وهمية (**Test Doubles**) للاختبار.

{{code:app/src/test/java/com/example/fakestore/data/auth/TestDoubles.kt}}

### الشرح
**`FakeClock`:** ساعة لا تتحرك إلا بأمر منا.

- `advanceBy(61_000)` = "مرت 61 ثانية" **فوراً**.

**`InMemoryTokenStorage`:** تخزين في الذاكرة بدل التشفير.

- `@Volatile`: يضمن أن كل الـ threads ترى آخر قيمة مكتوبة.

**`FakeStoreServer`:** يستخدم **MockWebServer**، وهو سيرفر HTTP حقيقي يعمل على الكمبيوتر محلياً ويقلّد FakeStore:

- `/auth/login`: يرد `token-1` ثم `token-2`… ويعدّ كم مرة استُدعي (`loginCount`).
- `/products`: يرد بمنتجات، ويسجّل الـ header الذي وصله (`productAuthHeaders`).
- `loginDelayMillis`: يؤخر رد login، ليبقى التجديد "جارياً" بينما تصل الطلبات الأخرى. **ضروري لاختبار التزامن بصدق.**
- `loginResponseCode` / `productsResponseCode`: لمحاكاة الرفض (401).

**`productApi(authManager)`:** يبني عميلاً محمياً **بنفس طريقة** `NetworkModule`، فنختبر التركيب الحقيقي نفسه.

## 51. `test/.../AuthManagerTest.kt`
**الدور:** اختبار منطق `AuthManager` مباشرة.

{{code:app/src/test/java/com/example/fakestore/data/auth/AuthManagerTest.kt}}

### الشرح
| الاختبار | ماذا يثبت |
|---|---|
| `login stores token and tokenSavedAt` | الدخول يحفظ الـ token مع وقت الحفظ |
| `login with wrong password…` | كلمة مرور خاطئة تعطي `InvalidCredentialsException`، ولا يُحفظ شيء |
| `valid token is returned without any network call` | عند 59 ثانية: لا يوجد أي طلب شبكة |
| `token is treated as expired after exactly 60 seconds` | عند **60 ثانية بالضبط**: تجديد، و`tokenSavedAt` جديد |
| `clock moving backwards…` | تغيير ساعة الجهاز للخلف يعني اعتبار الـ token منتهياً |
| ★ `concurrent callers … exactly one refresh` | **10 threads معاً** تنتج **login واحداً**، وكلها تحصل على نفس الـ token |
| `rejected refresh forces logout…` | رفض التجديد يعني مسح الجلسة، وإرسال الحدث، و**محاولة واحدة فقط** |
| `network failure during refresh keeps the session` | انقطاع الإنترنت لا يُخرج المستخدم |
| `server outage during login…` | تعطّل السيرفر (521) أثناء الدخول يعطي `ServerUnavailableException`، لا "كلمة مرور خاطئة" |
| `server outage during refresh keeps the session…` | تعطّل السيرفر (503) أثناء التجديد لا يُخرج المستخدم، ومحاولة واحدة فقط |
| `no session means SessionExpiredException…` | بلا جلسة: خطأ دون أي طلب |
| `logout during a refresh…` | Logout أثناء التجديد لا يعيد الجلسة |

**كيف يعمل اختبار التزامن؟**

1. `Executors.newFixedThreadPool(10)`: عشرة threads حقيقية.
2. `CountDownLatch(1)`: "خط البداية". كل الـ threads تنتظر عنده.
3. `start.countDown()`: تنطلق العشرة في نفس اللحظة.
4. التحقق: `loginCount == 1`، وكل النتائج تساوي `token-1`.

**`runTest`:** يشغّل كود coroutines في الاختبار.
**`backgroundScope`:** يستمع للأحداث في الخلفية، ويُلغى تلقائياً في نهاية الاختبار.

## 52. `test/.../AuthInterceptorTest.kt`
**الدور:** اختبار **متكامل** (**integration test**): Retrofit + OkHttp + Interceptor + AuthManager معاً، أمام سيرفر محلي.

{{code:app/src/test/java/com/example/fakestore/data/auth/AuthInterceptorTest.kt}}

### الشرح
| الاختبار | ماذا يثبت |
|---|---|
| `every product request carries the bearer token` | كل طلب منتجات وصل للسيرفر ومعه `Bearer abc` |
| `expired token is refreshed before the request is sent` | السيرفر **لم يستقبل أبداً** `Bearer old` المنتهي، بل `Bearer token-1` الجديد مباشرة |
| ★ `parallel requests … share one refresh` | 5 طلبات Retrofit متوازية تنتج login واحداً، و5 طلبات منتجات، كلها بالـ token الجديد |
| `without a session the request fails…` | بلا جلسة: الطلب **لا يصل للسيرفر أصلاً** |

## 53. `test/.../TokenAuthenticatorTest.kt`
**الدور:** إثبات عدم وجود **حلقة لا نهائية** عند رد 401.

{{code:app/src/test/java/com/example/fakestore/data/auth/TokenAuthenticatorTest.kt}}

### الشرح
**السيناريو:** السيرفر يرد **401 دائماً**.

**النتيجة المثبتة:**

- طلبان فقط: الأصلي + **إعادة واحدة**.
- طلب login واحد.
- الجلسة مُسحت (خروج إجباري).
- الـ headers: الأول `Bearer rejected`، والثاني `Bearer token-1`.

بدون فحص `priorResponse` كانت المحاولات ستتكرر حتى حد OkHttp الداخلي (20 محاولة).

---

## الجزء الثامن: الصورة الكاملة

## 54. رحلة طلب كاملة عبر الملفات
**السيناريو:** المستخدم سجّل دخوله قبل 70 ثانية، ثم ضغط على منتج.

| # | أين | ماذا يحدث |
|---|---|---|
| 1 | `ProductAdapter` | الضغط يستدعي `navigate(action_products_to_detail, productId=3)` |
| 2 | `nav_graph.xml` | يفتح `ProductDetailFragment` |
| 3 | `ProductDetailViewModel` | يقرأ `productId` من `SavedStateHandle`، ثم `loadProduct()`، والحالة `Loading` |
| 4 | `ProductRepositoryImpl` | `api.getProduct(3)` |
| 5 | `ProductApi` (Retrofit) | يبني طلب `GET /products/3` على العميل `@AuthClient` |
| 6 | `AuthInterceptor` | `authManager.getValidToken()` |
| 7 | `AuthManager` | المسار السريع: عمر الـ token 70 ثانية، **منتهٍ** |
| 8 | `AuthManager` | يدخل `refreshLock` ويتحقق مرة ثانية: ما زال منتهياً |
| 9 | `AuthManager.refresh` | `requestNewSession` يرسل `AuthApi.login` على العميل `@PlainClient`، **بلا Interceptor** |
| 10 | `EncryptedTokenStorage` | يحفظ الـ token الجديد مشفّراً مع `tokenSavedAt = الآن` |
| 11 | `AuthInterceptor` | يضيف `Authorization: Bearer <جديد>` ويرسل الطلب |
| 12 | Gson | يحوّل JSON إلى `ProductDto`، ثم `toDomain()` إلى `Product` |
| 13 | `ProductDetailViewModel` | الحالة `Success(product)` |
| 14 | `ProductDetailFragment` | `render` ثم `showProduct`، فتظهر التفاصيل |

**ولو رفض السيرفر بيانات الدخول في الخطوة 9:**

1. `forceLogout()`: يمسح الجلسة ويرسل `SessionExpired`.
2. يُرمى `SessionExpiredException` (وهو `IOException`)، فتعرض الشاشة "انتهت الجلسة".
3. `MainActivity` تستقبل الحدث، فتعرض Toast وتنتقل لـ Login مع مسح كل الشاشات.

## 55. كيف تعرض المشروع للمدير (Demo)
1. شغّل التطبيق، وافتح نافذة **Logcat** في Android Studio، واكتب في البحث:
   ```
   okhttp
   ```

2. سجّل الدخول بـ `mor_2314` وكلمة المرور `83r5^_`. سيظهر:
   ```
   --> POST https://fakestoreapi.com/auth/login
   ```

3. تصفّح المنتجات خلال أول دقيقة: تظهر طلبات `GET /products` فقط، **بلا login**.
4. انتظر أكثر من 60 ثانية، ثم من القائمة (⋮) اختر **Run concurrency test**. سيظهر:
   - `POST /auth/login` **مرة واحدة فقط**.
   - ثم 5 طلبات `GET /products/1..5`.
5. اختر **Log out**: تعود لشاشة Login، وزر الرجوع يغلق التطبيق.
6. افصل الإنترنت ثم اختر **Refresh**: رسالة "No internet connection" مع زر Retry، ولا يحدث crash.
   > ⚠️ **هذه الخطوة لا تعمل في الوضع التجريبي:** السيرفر الوهمي داخل الهاتف، فلا يحتاج إنترنت، وتظهر المنتجات حتى مع فصل الشبكة (فقط الصور لا تظهر). انظر "حدود السيرفر الوهمي" في الجزء التاسع.
7. شغّل الاختبارات بـ `./gradlew test`، واعرض نتيجة الـ 21 اختباراً.

## 56. تقابل المتطلبات مع الكود
| المتطلب في ملف المهمة | الملف | الكود |
|---|---|---|
| Username & password inputs, Login button | `fragment_login.xml` | `username_input`, `password_input`, `login_button` |
| Loading & error states | `LoginViewModel` / `LoginFragment` | `LoginUiState`, `render()` |
| Securely store the token | `EncryptedTokenStorage` | AES-256 + Android Keystore |
| Authorization Interceptor | `AuthInterceptor` | `.header("Authorization", "Bearer …")` |
| Token validity: 60 seconds | `AuthManager` | `TOKEN_LIFETIME_MS = 60_000L` |
| Store tokenSavedAt | `AuthSession` | `savedAtMillis` |
| Treat token as expired after 60 s | `AuthManager` | `isExpired()` |
| Do not send requests with an expired token | `AuthManager` | `getValidToken()` قبل كل طلب |
| Trigger refresh flow automatically | `AuthManager` | `refresh()` |
| Only one refresh request must run | `AuthManager` | `synchronized(refreshLock)` + التحقق الثاني |
| Other requests wait and resume | `AuthManager` | الانتظار على القفل ثم المتابعة بالـ token الجديد |
| Simulate refresh by re-calling login | `AuthManager` | `requestNewSession()` → `AuthApi.login` |
| Encapsulate inside AuthManager | `AuthManager` | كل المنطق في كلاس واحد |
| Avoid infinite retry loops | `NetworkModule` / `AuthManager` / `TokenAuthenticator` | عميل منفصل + محاولة واحدة + `priorResponse` |
| If refresh fails → force logout | `AuthManager` / `MainActivity` | `forceLogout()` → `SessionExpired` |
| Products list (RecyclerView) | `ProductAdapter` / `fragment_products.xml` | `ListAdapter` + `DiffUtil` |
| Product details screen | `ProductDetailFragment` | |
| Logout: clear token, go to Login | `ProductsViewModel` / `nav_graph.xml` | `logout()` + `action_global_login` |
| Architecture: MVVM | كل الشاشات | Fragment + ViewModel + Repository |
| Networking: Retrofit + OkHttp | `NetworkModule` | |
| (إضافة) رسالة دقيقة عند تعطّل السيرفر | `AuthExceptions` / `ErrorMessages` | `ServerUnavailableException` → "The server is unavailable right now" |
| (إضافة) تشغيل التطبيق رغم تعطّل FakeStore | `MockBackendInterceptor` / `gradle.properties` | `fakestore.useMockBackend=true` (نسخة التطوير فقط) |

---

## الجزء التاسع: الوضع التجريبي (Mock Backend)

**لماذا أضفناه؟** أثناء العمل تعطّل سيرفر FakeStore **للجميع**، وكانت Cloudflare تعرض الخطأ `Host Error`. بدون سيرفر لا يمكن اختبار تسجيل الدخول ولا المنتجات ولا منطق الـ Token على الهاتف.

**الحل الاحترافي:** سيرفر وهمي **داخل التطبيق**، لنسخة التطوير فقط. هذا ما تفعله الفرق عادةً حتى لا يتوقف عملها بسبب خدمة خارجية.

**المبدأ الأهم:** السيرفر الوهمي يستبدل **الطرف البعيد فقط**. كل شيء آخر يعمل كما هو في الإنتاج:

| الجزء | مع السيرفر الوهمي |
|---|---|
| `AuthInterceptor` وإضافة `Bearer` | يعمل كما هو |
| انتهاء الـ Token بعد 60 ثانية | يعمل كما هو |
| القفل والتجديد الواحد | يعمل كما هو |
| الخروج الإجباري ورسائل الخطأ | تعمل كما هي |
| **الرد على الطلب** | **من داخل الهاتف بدل fakestoreapi.com** |

**التفعيل والإيقاف:** سطر واحد في `gradle.properties`:
```
fakestore.useMockBackend=true
```
بعد تغييره اضغط **Sync Now**، ثم شغّل التطبيق من جديد.

## 57. `data/mock/MockProducts.kt`
**الدور:** بيانات المنتجات التي يردّ بها السيرفر الوهمي.

{{code:app/src/main/java/com/example/fakestore/data/mock/MockProducts.kt}}

### الشرح
- `internal object`: كائن وحيد، ومرئي داخل الوحدة `app` فقط.
- `items`: عشرة منتجات بنفس أسماء وأسعار وتصنيفات FakeStore الحقيقية تقريباً.
- `listJson()` و`productJson(id)`: تبني **نص JSON بنفس شكل رد FakeStore بالضبط**، فيحوّله Gson إلى `ProductDto` دون أي تغيير في باقي الكود.
- **الصور من `picsum.photos`:** لأن صور FakeStore موجودة على نفس السيرفر المتعطّل.
- `quote()`: تضع النص بين علامتي تنصيص، وتهرّب الرموز الخاصة، حتى يبقى JSON صحيحاً.

## 58. ★ `data/mock/MockBackendInterceptor.kt`
**الدور:** السيرفر الوهمي نفسه.

{{code:app/src/main/java/com/example/fakestore/data/mock/MockBackendInterceptor.kt}}

### الشرح
**كيف "يردّ" Interceptor بدل السيرفر؟** أي Interceptor عادي يستدعي `chain.proceed(request)` ليمرّر الطلب إلى الأمام. هذا الكلاس **لا يستدعيها أبداً**، بل يبني بنفسه `Response` كاملاً، بكود وجسم JSON، ويعيده. فيظن OkHttp وRetrofit أن الرد جاء من الإنترنت.

**`intercept()`:** يقرأ الطريقة (`GET`/`POST`) والمسار، ويوجّه الطلب:

| الطلب | الدالة |
|---|---|
| `POST /auth/login` | `login()` |
| `GET /products` أو `GET /products/{id}` | `products()` |
| أي شيء آخر | 404 |

**`login()`:**

- `loginCount.incrementAndGet()`: يعدّ طلبات الدخول. تستخدمه الاختبارات لإثبات حدوث **تجديد واحد فقط**.
- `Thread.sleep(loginDelayMillis)`: **تأخير مقصود** (800 ميلي ثانية). بدونه يكون الرد فورياً، فلا تجتمع الطلبات المتزامنة عند القفل، ويصبح اختبار التزامن بلا معنى.
- يقرأ جسم الطلب، ويحوّله بـ Gson إلى `LoginRequest`:
    - بيانات صحيحة: `200` مع `{"token":"mock-token-N"}`، فكل تجديد يعطي token برقم جديد.
    - بيانات خاطئة: `401`، تماماً مثل FakeStore.

**`products()`:**

- **يتحقق من وجود `Bearer` token فعلاً.** إذا غاب يرد `401`. هذا أكثر صرامة من FakeStore الحقيقي، الذي لا يتحقق أصلاً.
- `/products`: يرجع القائمة كاملة.
- `/products/{id}`: يرجع المنتج، أو `404` إذا لم يوجد.

**`Response.Builder()`:** يبني الرد: الطلب الأصلي، والبروتوكول، والكود، والرسالة، والجسم بنوع `application/json`.

## 59. `test/.../MockBackendInterceptorTest.kt`
**الدور:** إثبات أن التطبيق كله يعمل فوق السيرفر الوهمي.

{{code:app/src/test/java/com/example/fakestore/data/mock/MockBackendInterceptorTest.kt}}

### الشرح
يبني الاختبار العميلين **بنفس ترتيب `NetworkModule`**: `AuthInterceptor`، ثم السيرفر الوهمي في الآخر.

| الاختبار | ماذا يثبت |
|---|---|
| `login and products work end to end` | الدخول ثم القائمة (10 منتجات) ثم منتج واحد |
| `wrong password is rejected` | كلمة مرور خاطئة تعطي `401` و`InvalidCredentialsException` |
| ★ `expired token with parallel requests…` | بعد 61 ثانية: 5 طلبات متزامنة تنتج **login واحداً إضافياً فقط** (`loginCount == 2`: الدخول الأول + تجديد واحد) |
| `unknown product returns 404` | منتج غير موجود يعطي `404` |

### حدود السيرفر الوهمي: ما الذي لا يمكن رؤيته على الهاتف؟
السيرفر الوهمي يرد دائماً بنجاح على البيانات الصحيحة، لذلك بعض الحالات **لا تظهر على الهاتف** في الوضع التجريبي:

| الحالة | لماذا لا تظهر | أين أثبتناها |
|---|---|---|
| الخروج الإجباري عند رفض التجديد | السيرفر الوهمي لا يرفض أبداً بيانات دخول محفوظة وصحيحة | `rejected refresh forces logout…` في القسم 51 |
| رسالة "لا يوجد إنترنت" | الرد يأتي من داخل الهاتف، فالشبكة غير مستخدمة | `network failure during refresh…` في القسم 51 |
| رسالة "السيرفر غير متاح" | السيرفر الوهمي لا يتعطّل | `server outage…` في القسم 51 |
| `TokenAuthenticator` عند رد 401 | انظر الشرح التقني أدناه | القسم 53 |

**الشرح التقني لسطر `TokenAuthenticator`:**

- OkHttp يستدعي الـ `Authenticator` من حلقة داخلية اسمها `RetryAndFollowUpInterceptor`، وهي تعمل **بعد** كل الـ Interceptors التي نضيفها بـ `addInterceptor`.
- السيرفر الوهمي واحد من هذه الـ Interceptors، ويرد **قبل** الوصول إلى تلك الحلقة.
- لذلك لو رد السيرفر الوهمي بـ 401، لا يرى OkHttp ذلك كرد من الشبكة، ولا يستدعي `TokenAuthenticator`.
- عملياً لا يحدث هذا أبداً، لأن `AuthInterceptor` يضيف الـ token دائماً قبل السيرفر الوهمي.

**الخلاصة للمقابلة:** "الهاتف يعرض المسار الطبيعي والتزامن. أما مسارات الفشل فمثبتة بالاختبارات، لأنها أصعب في الإنتاج يدوياً على الهاتف، والاختبار يكررها بدقة في كل مرة."

## 60. كيف تعرض الوضع التجريبي للمدير
1. افتح التطبيق: سيظهر الشريط الملوّن أعلى شاشة الدخول:
    ```
    Demo mode: using a built-in mock server instead of fakestoreapi.com
    ```
2. قل للمدير بوضوح: "سيرفر FakeStore تعطّل للجميع أثناء العمل، فأضفت سيرفراً وهمياً لنسخة التطوير فقط، يستبدل الطرف البعيد ويُبقي منطق الـ Token حقيقياً. نسخة الإنتاج تستخدم FakeStore دائماً."
3. اعرض الخطوات نفسها من القسم 55. في Logcat ستظهر الطلبات كأنها ذهبت إلى `fakestoreapi.com`، لكن الرد يأتي من داخل الهاتف.
4. عندما يعود FakeStore: غيّر المفتاح إلى `false`، واضغط **Sync Now**، فيختفي الشريط ويعود التطبيق للسيرفر الحقيقي.

---

## الجزء العاشر ★: فهم المنطق بالسيناريوهات

هذا الجزء يشرح **ماذا يحدث** في التطبيق كقصة، خطوة بخطوة، دون الدخول في الكود. كل خطوة تذكر الملف المسؤول عنها، فإذا أردت رؤية الكود ارجع لقسمه.

## 61. المنطق كله في سبع جمل
1. عند الدخول نحفظ **ثلاثة أشياء** مشفّرة: الـ token، ووقت حفظه (`tokenSavedAt`)، وبيانات الدخول.
2. كل طلب منتجات يمر بـ `AuthInterceptor`، وهو يسأل `AuthManager`: "أعطني token صالحاً".
3. `AuthManager` يحسب العمر: `الآن - tokenSavedAt`. أقل من 60 ثانية يعني صالح، فيعيده فوراً.
4. إذا كان منتهياً، يجدّده بإرسال login من جديد بالبيانات المحفوظة، **قبل** إرسال الطلب.
5. التجديد داخل **قفل**: أول طلب يجدّد، والبقية ينتظرون ثم يأخذون الـ token الجديد **دون** تجديد آخر.
6. طلب login نفسه يمر بعميل **منفصل** بلا `AuthInterceptor`، فلا يمكن أن يطلب تجديداً، **فلا حلقة لا نهائية**.
7. إذا رفض السيرفر التجديد: خروج إجباري. وإذا كانت المشكلة في الشبكة أو السيرفر: تبقى الجلسة، وتظهر رسالة.

## 62. السيناريوهات خطوة بخطوة

### السيناريو 1: فتح التطبيق
| # | الملف | ماذا يحدث |
|---|---|---|
| 1 | `MainActivity` | تسأل `authManager.isLoggedIn()`: هل توجد جلسة محفوظة؟ |
| 2 | `EncryptedTokenStorage` | يفك تشفير الملف ويقرأ الحقول |
| 3 | `MainActivity` | **توجد جلسة:** تبدأ بشاشة المنتجات. **لا توجد:** تبدأ بشاشة Login |

**ملاحظة:** لا نتحقق هنا من انتهاء الـ token. حتى لو كان منتهياً منذ ساعات، سيُجدَّد تلقائياً مع أول طلب (السيناريو 4).

### السيناريو 2: تسجيل الدخول
| # | الملف | ماذا يحدث |
|---|---|---|
| 1 | `LoginFragment` | المستخدم يضغط الزر، فيُرسل النصين للـ ViewModel |
| 2 | `LoginViewModel` | الحقول فارغة؟ رسالة خطأ **دون** إرسال. غير ذلك: الحالة `isLoading = true` |
| 3 | `AuthManager.login` | ينتقل إلى thread خلفي (`Dispatchers.IO`) |
| 4 | `requestNewSession` | يرسل `POST /auth/login` على العميل **العادي** |
| 5 | `requestNewSession` | 200 مع token: ينشئ `AuthSession` بوقت **الآن**. 4xx: `InvalidCredentialsException`. 5xx: `ServerUnavailableException` |
| 6 | `saveIfStillCurrent` | يحفظ الجلسة مشفّرة |
| 7 | `LoginViewModel` | `isLoggedIn = true`، أو رسالة الخطأ المناسبة |
| 8 | `LoginFragment` | ينتقل للمنتجات، ويحذف Login من سجل الرجوع |

### السيناريو 3: طلب عادي والـ token صالح (عمره 20 ثانية)
| # | الملف | ماذا يحدث |
|---|---|---|
| 1 | `ProductsViewModel` | `repository.getProducts()` |
| 2 | `AuthInterceptor` | `getValidToken()` |
| 3 | `AuthManager` | **المسار السريع:** العمر 20 ثانية، أقل من 60، فيعيد الـ token **دون أي قفل** |
| 4 | `AuthInterceptor` | يضيف `Authorization: Bearer <token>` ويرسل |

هذه هي الحالة الأكثر حدوثاً، ولهذا جعلناها بلا قفل ولا انتظار.

### السيناريو 4: طلب واحد والـ token منتهٍ (عمره 75 ثانية)
| # | الملف | ماذا يحدث |
|---|---|---|
| 1 | `AuthManager` | المسار السريع: 75 ≥ 60، **منتهٍ** |
| 2 | `AuthManager` | يدخل القفل `refreshLock` |
| 3 | `AuthManager` | التحقق الثاني: ما زال منتهياً، فيجدّد |
| 4 | `refresh` | login جديد بالبيانات المحفوظة، ثم حفظ الـ token الجديد بوقت **الآن** |
| 5 | `AuthInterceptor` | يرسل الطلب بالـ token **الجديد** |

**النقطة المهمة:** السيرفر **لم يستقبل أبداً** الـ token المنتهي. التجديد حدث **قبل** الإرسال، لا بعد الفشل.

### السيناريو 5: خمسة طلبات معاً والـ token منتهٍ (اختبار التزامن)
| الزمن | الطلب 1 | الطلبات 2 إلى 5 |
|---|---|---|
| 0 ms | منتهٍ، يدخل القفل 🔒 | منتهٍ، تنتظر عند القفل ⏳ |
| 0 ms | التحقق الثاني: منتهٍ، يرسل login | تنتظر ⏳ |
| 800 ms | يصل الرد، يحفظ الـ token الجديد، يخرج 🔓 | تنتظر ⏳ |
| 800 ms | يرسل طلبه ✅ | تدخل **واحداً تلو الآخر**، والتحقق الثاني يجد token **صالحاً**، فتخرج فوراً وترسل ✅ |

**النتيجة في Logcat:** سطر login **واحد**، ثم 5 طلبات منتجات.

**لماذا التحقق الثاني هو السر؟** بدونه، كل طلب ينتظر دوره ثم يجدّد بنفسه: 5 طلبات login متتالية. جربنا حذفه في الاختبارات، فأنتجت 10 طلبات متزامنة 10 طلبات login.

### السيناريو 6: السيرفر يرفض التجديد (مثلاً تغيّرت كلمة المرور)
| # | الملف | ماذا يحدث |
|---|---|---|
| 1 | `requestNewSession` | السيرفر يرد 401، فيُرمى `InvalidCredentialsException` |
| 2 | `refresh` | يلتقطه، ويستدعي `forceLogout()` |
| 3 | `forceLogout` | يمسح الجلسة، ثم يرسل الحدث `SessionExpired` |
| 4 | `refresh` | يرمي `SessionExpiredException`، **دون إعادة محاولة** |
| 5 | `ProductsViewModel` | يعرض "Your session has expired" |
| 6 | `MainActivity` | تستقبل الحدث: Toast، ثم انتقال إلى Login مع مسح كل الشاشات |

**لماذا لا نعيد المحاولة؟** لأن البيانات نفسها سترفض مرة أخرى، وإعادة المحاولة هنا **هي** الحلقة اللانهائية التي تحذّر منها المهمة.

### السيناريو 7: انقطاع الإنترنت أثناء التجديد
| # | الملف | ماذا يحدث |
|---|---|---|
| 1 | `requestNewSession` | OkHttp يرمي `IOException` (لا يوجد اتصال) |
| 2 | `refresh` | **لا يلتقطه**: يمر للطلب كما هو. الجلسة **لا تُمسح** |
| 3 | `ErrorMessages` | يحوّله إلى "No internet connection" |
| 4 | المستخدم | يضغط Retry بعد عودة الإنترنت، فيحدث التجديد بنجاح |

**القرار:** المستخدم لم يخطئ، والمشكلة مؤقتة، فلا نجبره على تسجيل الدخول من جديد. نفس الشيء عند تعطّل السيرفر (5xx).

### السيناريو 8: المستخدم يضغط Logout أثناء تجديد جارٍ
| # | من | ماذا يحدث |
|---|---|---|
| 1 | Thread التجديد | يحفظ رقم الجيل الحالي (مثلاً 4) ثم يرسل login |
| 2 | المستخدم | يضغط Logout: الجيل يصبح 5، والجلسة تُمسح |
| 3 | Thread التجديد | يصل الرد بـ token جديد |
| 4 | `saveIfStillCurrent` | يقارن: 4 ≠ 5، إذن حدث خروج. **لا يحفظ**، ويرمي `SessionExpiredException` |

**بدون رقم الجيل:** يُحفظ الـ token الجديد، **فيعود المستخدم مسجّلاً للدخول بعد أن خرج**.

### السيناريو 9: السيرفر يرد 401 على طلب منتجات (سيرفر حقيقي صارم)
| # | الملف | ماذا يحدث |
|---|---|---|
| 1 | OkHttp | يرى 401، فيستدعي `TokenAuthenticator` |
| 2 | `TokenAuthenticator` | هل هذه إعادة محاولة سابقة (`priorResponse`)؟ لا |
| 3 | `refreshAfterUnauthorized` | هل جدّد طلب آخر قبلنا؟ إذا نعم نأخذ الـ token الموجود. إذا لا نجدّد |
| 4 | OkHttp | يعيد الطلب **مرة واحدة** بالـ token الجديد |
| 5 | إذا رد 401 مرة ثانية | `priorResponse` موجود الآن: خروج إجباري، **ولا محاولة ثالثة** |

FakeStore لا يرد بـ 401 أبداً، فهذا السيناريو "شبكة أمان" للسيرفرات الحقيقية.

### السيناريو 10: إغلاق التطبيق دون Logout ثم فتحه
| # | الملف | ماذا يحدث |
|---|---|---|
| 1 | النظام | التطبيق يُغلق. الجلسة **باقية** في الملف المشفّر على القرص |
| 2 | `MainActivity` | عند الفتح: `isLoggedIn()` يجد جلسة، فيبدأ بالمنتجات مباشرة |
| 3 | `AuthManager` | أول طلب: إذا مرت أكثر من 60 ثانية، تجديد واحد تلقائي (السيناريو 4) |

**لماذا `currentTimeMillis` وليس `elapsedRealtime`؟** لأن الثاني يعود للصفر عند إعادة تشغيل الهاتف، فيصبح `tokenSavedAt` المحفوظ بلا معنى.

## 63. اختبر فهمك (أسئلة متوقعة في المقابلة)
**س: لماذا عميلا OkHttp وليس عميلاً واحداً؟**

ج: لو مر طلب login بالـ `AuthInterceptor`، لطلب token صالحاً، والـ token منتهٍ، فيطلب تجديداً، والتجديد هو login، فيمر بالـ Interceptor مرة أخرى… حلقة لا نهائية. العميل المنفصل يقطعها من الأساس.

**س: لماذا `synchronized` وليس `Mutex`؟**

ج: `intercept()` دالة عادية تعمل على thread حقيقي من OkHttp، وليست `suspend`. `Mutex` يحتاج `suspend`، فكنا سنحتاج `runBlocking` داخل الـ Interceptor.

**س: لماذا المسار السريع خارج القفل؟**

ج: لأن 99% من الطلبات تجد token صالحاً. لو دخلت كلها القفل لانتظرت بعضها بلا سبب.

**س: ماذا لو حذفنا التحقق الثاني داخل القفل؟**

ج: كل طلب منتظر سيجدّد بعد دوره، فتصبح 5 طلبات = 5 login. الاختبار `concurrent callers … exactly one refresh` يكشف ذلك فوراً.

**س: لماذا ترث الأخطاء من `IOException`؟**

ج: لأنها تُرمى من داخل OkHttp. OkHttp يمرر `IOException` كفشل عادي للطلب، أما أي نوع آخر فيُسقط التطبيق.

**س: كيف تختبر انتهاء الصلاحية دون انتظار 60 ثانية؟**

ج: الوقت يأتي من interface اسمه `Clock`. في الاختبارات نستخدم `FakeClock` ونقدّمه 61 ثانية فوراً.

**س: لماذا تحفظ كلمة المرور؟ أليس هذا خطراً؟**

ج: FakeStore لا يوفّر refresh token، والمهمة تطلب التجديد بإعادة login. لذلك نحفظها **مشفّرة** بمفتاح في Android Keystore. في تطبيق حقيقي نحفظ refresh token من السيرفر، ولا نحفظ كلمة المرور أبداً.

**س: ما الفرق بين `AuthInterceptor` و`TokenAuthenticator`؟**

ج: الأول **استباقي**: يمنع إرسال token منتهٍ أصلاً. الثاني **تفاعلي**: يعالج رد 401 إذا رفض السيرفر token كنا نظنه صالحاً.

## 64. دليل اختبار التطبيق على الهاتف
**التحضير (مرة واحدة):**

1. افتح Logcat في Android Studio، واكتب في البحث:
   ```
   okhttp
   ```
2. تأكد أن الهاتف متصل عبر Wireless debugging، وأن اسمه ظاهر بجانب زر ▶️.
3. اضغط ▶️ Run.

**أشكال الأسطر في Logcat:**
```
--> POST https://fakestoreapi.com/auth/login (43-byte body)
<-- 200 OK https://fakestoreapi.com/auth/login (805ms, 24-byte body)
--> GET https://fakestoreapi.com/products
<-- 200 OK https://fakestoreapi.com/products (302ms, ...)
```
السهم `-->` هو الطلب، والسهم `<--` هو الرد. العنوان يبقى `fakestoreapi.com` حتى في الوضع التجريبي، لكن الرد يأتي من داخل الهاتف.

**قائمة الاختبارات:**

| # | الخطوات | النتيجة المتوقعة |
|---|---|---|
| 1 | افتح التطبيق | شاشة Login، وفي أعلاها شريط `Demo mode` |
| 2 | اضغط Log in والحقول فارغة | `Please enter your username and password.` ولا يظهر أي سطر في Logcat |
| 3 | اكتب اسم المستخدم `mor_2314` | **الحرف الأول يبقى صغيراً**، ولا تظهر اقتراحات (تعديل المراجعة) |
| 4 | كلمة مرور خاطئة، ثم Log in | دائرة تحميل، ثم `Invalid username or password.` وفي Logcat سطر `<-- 401` |
| 5 | كلمة المرور الصحيحة `83r5^_` | قائمة بـ 10 منتجات. Logcat: login **واحد**، ثم `GET /products` |
| 6 | اضغط منتجاً | شاشة التفاصيل: صورة، وعنوان، وسعر، وتقييم، ووصف |
| 7 | ارجع، ثم ⋮ ← Refresh **خلال أول 60 ثانية** | Logcat: `GET /products` فقط، **بلا login** |
| 8 | انتظر أكثر من 60 ثانية، ثم ⋮ ← Run concurrency test | Logcat: login **واحد** ثم 5 طلبات `GET /products/1` إلى `/5`. رسالة أسفل الشاشة: `5 parallel requests finished…` |
| 9 | أعد الاختبار 8 مباشرة (قبل مرور 60 ثانية) | 5 طلبات منتجات **بلا login**، لأن الـ token جديد |
| 10 | ★ أغلق التطبيق **دون Logout** (اسحبه من قائمة التطبيقات الأخيرة)، ثم افتحه | يفتح على **المنتجات مباشرة** دون شاشة Login |
| 11 | بعد الاختبار 10: إذا مرت أكثر من 60 ثانية على آخر login | Logcat: login **واحد** تلقائي قبل `GET /products` |
| 12 | في شاشة المنتجات اضغط زر الرجوع في الهاتف | يُغلق التطبيق، **ولا** يعود لشاشة Login |
| 13 | ⋮ ← Log out | شاشة Login. زر الرجوع الآن يُغلق التطبيق |
| 14 | أغلق التطبيق وافتحه بعد Logout | يفتح على **Login**، لأن الجلسة مُسحت |
| 15 | على الكمبيوتر، في Terminal الخاص بـ Android Studio: `.\gradlew.bat test` | `BUILD SUCCESSFUL`، والـ 21 اختباراً ناجحة |

**إذا فشل أي اختبار:** صوّر الشاشة، وانسخ أسطر Logcat التي تبدأ بـ `-->` و`<--`، وأرسلها.

## 65. نتائج المراجعة الشاملة
**ما الذي رُوجع:**

- كل ملفات المشروع (56 ملفاً): الكود، وملفات XML، وGradle، والاختبارات.
- الاختبارات الـ 21 أعيد تشغيلها بعد التعديلات: **كلها ناجحة**.
- كل متطلب في ملف المهمة له كود يقابله (القسم 56).

**ما الذي عُدّل:**

| التعديل | الملف | السبب |
|---|---|---|
| `@Synchronized` على `read` و`save` و`clear` | `EncryptedTokenStorage` | منع قراءة جلسة نصف مكتوبة (القسم 16) |
| حقل اسم المستخدم بلا تكبير ولا تصحيح | `fragment_login.xml` | المشكلة التي واجهناها مع لوحة مفاتيح Infinix (القسم 38) |
| تنبيه أن فصل الإنترنت لا يظهر خطأ في الوضع التجريبي | هذا الملف، القسم 55 | كان الشرح يعد بنتيجة لن تظهر |
| جدول "حدود السيرفر الوهمي" | هذا الملف، الجزء التاسع | توضيح ما يُرى على الهاتف وما يُثبت بالاختبارات |

**حدود معروفة ومقصودة (قلها بصراحة إذا سُئلت):**

1. **كلمة المرور محفوظة** (مشفّرة): ضرورية لمحاكاة التجديد، ولا تُستخدم في تطبيق حقيقي.
2. **`EncryptedSharedPreferences` أوقفت Google تطويرها:** خلف interface، ويمكن استبدالها بسطر واحد.
3. **قراءة الجلسة عند فتح التطبيق تحدث على الـ Main thread:** تستغرق أجزاء من الثانية. في تطبيق كبير نقرأها في الخلفية مع شاشة بداية (**Splash screen**).
4. **مسارات الفشل لا تُعرض على الهاتف في الوضع التجريبي:** مثبتة بالاختبارات (الجزء التاسع).
