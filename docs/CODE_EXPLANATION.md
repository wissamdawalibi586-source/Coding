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
│   └── repository/                 ProductRepository
├── di/                             Hilt modules
└── ui/
    ├── common/                     UiState, errors, helpers
    ├── login/                      Login screen
    ├── products/                   list (RecyclerView)
    └── detail/                     product details
app/src/main/res/                   layouts, navigation, menu, strings
app/src/test/.../data/auth/         unit tests for the token logic
```

### ترتيب القراءة المقترح
اقرأ الأقسام بالترتيب، فكل قسم يعتمد على ما قبله:

| الجزء | المحتوى |
|---|---|
| الأول | إعدادات Gradle والـ Manifest |
| الثاني | نماذج البيانات والـ APIs |
| الثالث ★ | منطق الـ Token، وهو قلب المهمة |
| الرابع | الـ Repository وحقن الاعتماديات (Hilt) |
| الخامس | أدوات الواجهة المشتركة |
| السادس | الشاشات |
| السابع | الاختبارات |
| الثامن | الرحلة الكاملة للطلب، وكيف تعرض المشروع للمدير |

> **ملاحظة:** أرقام الأسطر الظاهرة بجانب الكود تساعدك على متابعة الشرح.

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

> **ملاحظة للمقابلة:** Google أوقفت تطوير مكتبة `security-crypto` (أصبحت **deprecated**). البديل الحديث هو **DataStore** مع تشفير بمفتاح من Keystore (مثلاً بمكتبة **Tink**). وبفضل `TokenStorage` كـ interface، يكفي كتابة كلاس جديد وتغيير سطر واحد في `DataModule`، دون لمس أي كلاس آخر.

## 17. `data/auth/AuthExceptions.kt`
**الدور:** أنواع الأخطاء الخاصة بالمصادقة.

{{code:app/src/main/java/com/example/fakestore/data/auth/AuthExceptions.kt}}

### الشرح
**`SessionExpiredException`:** لا توجد جلسة، أو السيرفر رفض التجديد. النتيجة: يجب تسجيل الدخول من جديد.

**`InvalidCredentialsException`:** السيرفر رفض اسم المستخدم أو كلمة المرور، أي رد بكود **4xx** مثل 401. نحتفظ بالكود في `httpCode`.

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
- **السطر 136:** أي فشل آخر (مثل 500، أي عطل في السيرفر) نرمي `IOException` عادياً. ليس ذنب المستخدم، فلا نُخرجه.
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
**الترتيب مهم: من الأكثر تحديداً إلى الأعم.** `InvalidCredentialsException` و`SessionExpiredException` يرثان من `IOException`. لو وضعنا `is IOException` أولاً، لظهرت لهما رسالة "لا يوجد إنترنت" الخاطئة.

| الخطأ | الرسالة |
|---|---|
| `InvalidCredentialsException` | اسم المستخدم أو كلمة المرور خاطئة |
| `SessionExpiredException` | انتهت الجلسة |
| `HttpException` (من Retrofit، أي رد بكود خطأ) | خطأ في السيرفر |
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
- `error_text`: رسالة الخطأ، مخفية (`gone`) حتى يحدث خطأ. `?attr/colorError` يجعل لونها أحمر من الثيم.
- `CircularProgressIndicator`: دائرة التحميل، مخفية في البداية.

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

**النتيجة:** **15 اختباراً، كلها ناجحة**.

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
7. شغّل الاختبارات بـ `./gradlew test`، واعرض نتيجة الـ 15 اختباراً.

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
