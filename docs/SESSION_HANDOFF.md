# ملف تسليم الجلسة (Session Handoff)

> **الغرض:** توثيق كامل لكل ما جرى في المحادثة السابقة، لمتابعة العمل في جلسة جديدة دون فقدان أي سياق.
> **آخر تحديث:** 2026-10-07 (الجلسة الثانية: المراجعة الشاملة)

---

## 0. كيف تبدأ جلسة جديدة (انسخ هذا النص كأول رسالة)

```
أكمل العمل على مهمة Android Test Task.
اقرأ أولاً الملف docs/SESSION_HANDOFF.md في الريبو
wissamdawalibi586-source/Coding على الفرع claude/pensive-mccarthy-ctuht8
فهو يحتوي كل السياق والقرارات وما تبقى.
اكتب ردودك بالعربية، وافصل الكلمات الإنجليزية في أسطر مستقلة.
```

---

## 1. من هو المستخدم وما تفضيلاته

| البند | التفاصيل |
|---|---|
| اللغة | العربية. يفهم المصطلحات الإنجليزية لكنه يحتاج شرحها |
| الخلفية | مطوّر Flutter ينتقل إلى Android Native |
| الهدف | تنفيذ مهمة اختبارية من مدير عمل **وفهمها جيداً**، لأن الحصول على الوظيفة يعتمد عليها |
| الجهاز | لابتوب Windows، و**Android Studio** (الواجهة الجديدة: القائمة مخفية خلف ☰) |
| الهاتف | **Infinix Smart 8** (يظهر باسم `INFINIX Infinix X6532`)، Android 13، متصل عبر **Wireless debugging** |
| المحاكي | **لا يستخدمه**: كان بطيئاً جداً ويجمّد اللابتوب |

### تفضيلات الكتابة (مهمة جداً)
1. **افصل الكلمات الإنجليزية عن العربية في أسطر مستقلة** في الردود، لأن خلط اللغتين في سطر واحد يربك الاتجاه في العرض.
2. ضع الأوامر والمسارات وأسماء الأزرار في **كتل كود مستقلة**.
3. اشرح خطوة بخطوة، بأرقام، ومع النتيجة المتوقعة لكل خطوة.
4. يريد **شرحاً لكل ملف كود في ملف PDF**، ويريد تحديث ملف الشرح مع كل تعديل.
5. يريد أن تصل التعديلات إلى Android Studio **تلقائياً عبر Git**، لا بالنسخ اليدوي.
6. **لا يريد إعادة تنزيل المشروع** إذا كان موجوداً على جهازه.

---

## 2. المهمة المطلوبة (ملخص ملف `Android_Test_Task.pdf`)

تطبيق Android بسيط يتعامل مع **FakeStore API**:
```
POST https://fakestoreapi.com/auth/login
GET  https://fakestoreapi.com/products
GET  https://fakestoreapi.com/products/{id}
```
بيانات حساب الاختبار:
```
mor_2314
83r5^_
```

**المتطلبات الإلزامية:**
1. شاشة دخول: حقلان، وزر، وحالات تحميل وخطأ، وحفظ الـ Token **بأمان**.
2. `Authorization Interceptor` يضيف `Bearer <token>` لكل طلب.
3. صلاحية الـ Token **60 ثانية**، وحفظ `tokenSavedAt`.
4. عدم إرسال Token منتهٍ، وتجديد تلقائي.
5. **التزامن:** عدة طلبات والـ Token منتهٍ تنتج **تجديداً واحداً فقط**، والباقي ينتظر.
6. التجديد = استدعاء `/auth/login` مرة أخرى، داخل `AuthManager`.
7. منع الحلقات اللانهائية، و**خروج إجباري** عند فشل التجديد.
8. قائمة منتجات بـ **`RecyclerView`**، وشاشة تفاصيل.
9. Logout: مسح الـ Token والعودة لشاشة الدخول.
10. **MVVM** و**Retrofit + OkHttp**.

**ملاحظة من المهمة:** FakeStore لا يتحقق فعلياً من الـ Token في المنتجات، لكن يجب أن يعامله التطبيق كأنه محمي.

---

## 3. الريبو والفروع

| البند | القيمة |
|---|---|
| الريبو | `wissamdawalibi586-source/Coding` |
| الفرع الحالي | `claude/pensive-mccarthy-ctuht8` (منذ الجلسة الثانية). الفرع القديم `claude/epic-cannon-vxutc4` توقف عند `ccfb1e8` |
| رابط النسخ | `https://github.com/wissamdawalibi586-source/Coding.git` |
| طلب دمج (PR) | **لم يُنشأ**. لا تنشئه إلا إذا طلبه المستخدم |

### سجل الـ commits
| الـ Commit | المحتوى |
|---|---|
| `1b6270d` / `36ed0d3` | مشروع تعليمي قديم (Posts App) ثم حذفه بطلب المستخدم. **غير مرتبط بالمهمة** |
| `2a3ecce` | `TASK_PLAN.md`: شرح المهمة وخطة التنفيذ وقاموس المصطلحات |
| `877c5fa` | `Android_Test_Task_Guide.pdf` + ملحق العمليات المعقدة |
| `677e10b` | **التنفيذ الكامل للتطبيق** + 15 اختباراً + ملف شرح الكود |
| `3555be4` | رسالة "السيرفر غير متاح" (`ServerUnavailableException`) + اختباران |
| `f9768a8` | **السيرفر الوهمي** (Mock Backend) لنسخة التطوير + 4 اختبارات |
| `96dfee5` | سجل التعديلات في ملف الشرح |
| `ccfb1e8` | هذا الملف (Session Handoff) |
| (الجلسة الثانية) | **المراجعة الشاملة:** `@Synchronized` في `EncryptedTokenStorage`، وحقل اسم المستخدم بلا تكبير، والجزء العاشر في ملف الشرح (سيناريوهات + دليل الاختبار + نتائج المراجعة)، وPDF جديد (97 صفحة) |

---

## 4. محتويات الريبو

| الملف أو المجلد | المحتوى |
|---|---|
| `app/` | كود التطبيق (Kotlin) |
| `README.md` | تعريف بالإنجليزية للمراجع: المعمارية والقرارات وطريقة العرض |
| `TASK_PLAN.md` | شرح المهمة والخطة وقاموس 70+ مصطلح (قبل التنفيذ) |
| `Android_Test_Task_Guide.pdf` | نسخة PDF من `TASK_PLAN.md` مع ملحق العمليات المعقدة |
| `docs/CODE_EXPLANATION.md` | **شرح الكود ملفاً ملفاً** (56 ملفاً، 10 أجزاء، 65 قسماً) |
| `docs/Code_Explanation.pdf` | نسخة PDF منه (97 صفحة) |
| `docs/SESSION_HANDOFF.md` | هذا الملف |

---

## 5. المعمارية المنفّذة

**التقنيات:**
```
Kotlin · MVVM · Retrofit + OkHttp · Hilt · Coroutines/Flow ·
Navigation Component · RecyclerView + ViewBinding ·
EncryptedSharedPreferences · Coil
```
**الإصدارات:** AGP `8.5.2`، Kotlin `2.0.20`، Gradle `8.9`، compileSdk `34`، minSdk `24`.

**الحزمة:** `com.example.fakestore`

```
app/src/main/java/com/example/fakestore/
├── FakeStoreApp.kt, MainActivity.kt
├── data/
│   ├── model/Product.kt
│   ├── remote/        AuthApi (Call), ProductApi (suspend), ApiConfig, dto/
│   ├── auth/          AuthManager ★, AuthInterceptor, TokenAuthenticator,
│   │                  TokenStorage, EncryptedTokenStorage, AuthSession, Clock,
│   │                  AuthExceptions, AuthEvent, AuthHeaders
│   ├── mock/          MockBackendInterceptor, MockProducts (dev only)
│   └── repository/    ProductRepository
├── di/                NetworkModule, DataModule, Qualifiers
└── ui/                common/, login/, products/, detail/
```

### أهم قرارات التصميم
1. **عميلا OkHttp:** `@PlainClient` لطلب login (بلا `AuthInterceptor`)، و`@AuthClient` للمنتجات. هذا يمنع الحلقة اللانهائية.
2. **Double-Checked Locking:** في `AuthManager.getValidToken()` مسار سريع بلا قفل، ثم `synchronized(refreshLock)` مع تحقق ثانٍ. استخدمنا `synchronized` وليس `Mutex` لأن الـ Interceptor دالة blocking.
3. **كل أخطاء المصادقة ترث من `IOException`:** حتى لا ينهار التطبيق عند رميها من داخل OkHttp.
4. **سياسة فشل التجديد:**
    - رفض البيانات (4xx): خروج إجباري عبر `AuthEvent.SessionExpired`.
    - انقطاع الشبكة أو تعطّل السيرفر (5xx): **الجلسة تبقى**، وتظهر رسالة مناسبة.
    - محاولة تجديد واحدة لكل طلب، فلا حلقات.
5. **`sessionGeneration`:** يمنع تجديداً بدأ قبل Logout من إعادة الجلسة بعده.
6. **حفظ كلمة المرور مشفّرة:** ضروري لمحاكاة التجديد، ومذكور في README أن التطبيق الحقيقي يستخدم refresh token.
7. **`TokenAuthenticator`:** شبكة أمان عند رد 401، يعيد المحاولة مرة واحدة فقط (`priorResponse`).
8. **`allowBackup="false"`** في الـ Manifest.
9. **أداة عرض للمراجع:** خيار في القائمة ⋮ اسمه `Run concurrency test` يطلق 5 طلبات متزامنة.

---

## 6. السيرفر الوهمي (Mock Backend)

**السبب:** في 2026-10-06 تعطّل `fakestoreapi.com` **للجميع** (Cloudflare: Host Error، وتأكدنا عبر downforeveryoneorjustme.com).

**الحل:** `MockBackendInterceptor` آخر Interceptor في السلسلة، يردّ بنفس شكل ردود FakeStore:
- login يتأخر 800ms عمداً، ليكون اختبار التزامن صادقاً.
- يتحقق فعلاً من وجود `Bearer` في طلبات المنتجات.
- 10 منتجات، وصورها من `picsum.photos`.

**التفعيل:** في `gradle.properties`:
```
fakestore.useMockBackend=true
```
- يتحوّل إلى `BuildConfig.USE_MOCK_BACKEND` في نسخة `debug` فقط. في `release` دائماً `false`.
- شاشة الدخول تُظهر شريطاً عند تفعيله:
  ```
  Demo mode: using a built-in mock server instead of fakestoreapi.com
  ```
- **عند عودة FakeStore:** غيّره إلى `false`، ثم Sync.

---

## 7. التحقق والاختبارات

**21 اختباراً ناجحاً** (JVM، بـ MockWebServer وFakeClock):

| الملف | العدد |
|---|---|
| `AuthManagerTest` | 12 |
| `AuthInterceptorTest` | 4 |
| `TokenAuthenticatorTest` | 1 |
| `MockBackendInterceptorTest` | 4 |

- **اختبار تحقق من قوة الاختبارات:** حذف التحقق الثاني داخل القفل جعل 10 طلبات متزامنة تنتج 10 طلبات login، فاكتُشف الخطأ.
- **بيئة Claude لا تستطيع بناء Android:** خوادم `dl.google.com` محجوبة عنها. لذلك تتحقق Claude من طبقة البيانات والاختبارات بمشروع Kotlin/JVM مؤقت في الـ scratchpad (بدون `EncryptedTokenStorage`)، بينما **البناء الكامل يحدث على جهاز المستخدم**.
- **على جهاز المستخدم:** `.\gradlew.bat test` نجح (67 مهمة)، والتطبيق يعمل على هاتفه.

---

## 8. نتائج الاختبار على الهاتف

| الاختبار | النتيجة |
|---|---|
| تسجيل الدخول | ✅ (بعد تصحيح خطأ كتابة: لوحة المفاتيح كبّرت أول حرف) |
| كلمة مرور خاطئة | ✅ رسالة `Invalid username or password` |
| قائمة المنتجات والتفاصيل | ✅ |
| **التزامن** | ✅ **login واحد ثم 5 طلبات منتجات** (مُثبت في Logcat الساعة 15:35:27) |
| Logout | ✅ |
| رسالة السيرفر المتعطّل | ✅ |
| **تذكّر الجلسة بعد إغلاق التطبيق** | ✅ (الجلسة الثانية): فتح على المنتجات، مع login تلقائي بعد 0.05 ثانية لأن الـ token انتهى |
| **دليل الاختبار الكامل (القسم 64، 15 اختباراً)** | ✅ **كلها ناجحة** على الهاتف (الجلسة الثانية) |
| التزامن بعد انتهاء الـ token | ✅ login واحد الساعة 15:13:59، ثم 5 طلبات بدأت كلها بعد رد login، بترتيب 1، 3، 4، 5، 2 |
| حقل الاسم بلا تكبير | ✅ الدخول نجح بالكتابة اليدوية. ملاحظة: الهاتف يعرض الملء التلقائي (Autofill)، وهذا مقصود |

---

## 9. مشاكل البيئة التي واجهناها وحلولها

| المشكلة | الحل |
|---|---|
| `Plugin com.android.application 8.5.2 was not found` عند أول Sync | تعثّر تحميل مؤقت. حُلّ بتشغيل `.\gradlew.bat help --refresh-dependencies --stacktrace` في Terminal |
| `curl -I` لا يعمل في Terminal | PowerShell يعتبر `curl` اسماً لأمر آخر. استخدم `curl.exe` |
| Gradle Sync معلّق 18 ساعة | نام الجهاز أثناء التحميل. أوقفه بـ ⏹، ثم `.\gradlew.bat --stop`، ثم `.\gradlew.bat test` |
| المحاكي: `hypervisor driver is not installed` | ثُبّت من رابط `Install Android Emulator hypervisor driver` |
| المحاكي بطيء جداً ويجمّد الجهاز | الانتقال إلى الهاتف الحقيقي |
| الهاتف لا يظهر عبر USB | استخدام **Wireless debugging** بدلاً منه |
| مسح رمز QR فتح Stack Overflow | المستخدم مسحه بكاميرا الهاتف. يجب المسح من داخل `Wireless debugging → Pair device with QR code` |
| `No internet connection` عند الدخول | FakeStore متعطّل (Cloudflare). أُضيفت رسالة دقيقة، ثم السيرفر الوهمي |
| `Invalid username or password` رغم البيانات الصحيحة | لوحة مفاتيح Infinix كبّرت أول حرف. طول الطلب (43 بايت) أثبت أن الخطأ حرف واحد |
| أخطاء `com.transsion` و`TranClassInfo` في Logcat | من نظام هاتف Infinix، **ليست من التطبيق**، تُتجاهل |
| اقتراح ترقية AGP | **رُفض عمداً**. لا تضغط `START AGP UPGRADE ASSISTANT` |
| المستخدم فتح مشروعاً فارغاً `D:\projectF\fakeStore` بالخطأ | مشروعنا اسمه `FakeStoreAuth`. يُفتح بـ `File → Open` وليس بإنشاء مشروع جديد |

---

## 10. سير العمل بين Claude والمستخدم

1. **Claude** تعدّل الكود، وتشغّل الاختبارات، وتحدّث `docs/CODE_EXPLANATION.md`، وتعيد بناء `docs/Code_Explanation.pdf`، ثم ترفع على الفرع.
2. **المستخدم** يضغط في Android Studio:
   ```
   Ctrl + T
   ```
   وإذا تغيّرت ملفات Gradle، يضغط **Sync**.
3. **المستخدم يشغّل** ▶️ على هاتفه، ويرسل صوراً أو نصوص Logcat.

**تنبيه:** المستخدم لا يعدّل الملفات بنفسه، لتجنّب التعارض.

### طريقة بناء ملف الـ PDF
- المصدر: `docs/CODE_EXPLANATION.md`. كل `{{code:path}}` فيه يُستبدل بالكود الحقيقي من الملف، ملوّناً بـ Pygments ومع أرقام الأسطر.
- يُحوَّل إلى HTML باتجاه RTL، ثم إلى PDF بـ Chromium (Playwright).
- الخطوط: `Noto Sans Arabic` و`Noto Sans` و`JetBrains Mono` (تُحمَّل من Google Fonts إلى ملفات محلية)، و`Noto Color Emoji` (مثبّت).
- أرقام الأسطر بنمط Pygments `linenos="inline"`، **وليس** `table`، لأن الجدول يمنع انقسام الكود بين الصفحات.
- مسار Chromium: `/opt/pw-browsers/chromium-1194/chrome-linux/chrome` (يُمرَّر كـ `executable_path`).
- الاختبارات في بيئة Claude: مشروع Kotlin/JVM في الـ scratchpad يشير إلى مجلدات `app/src` مباشرة. Maven Central قد يرد 429، فأعد المحاولة.
- **قواعد Markdown مهمة:** سطر فارغ قبل كل قائمة أو جدول، والقوائم المتداخلة بإزاحة 4 مسافات، وتجنّب المخططات التي تخلط العربية بالإنجليزية داخل كتل الكود (استخدم جداول بدلاً منها).
- **عند تعديل أي ملف كود:** حدّث أرقام الأسطر المذكورة في الشرح، خاصة في القسم 20 (`AuthManager`).

---

## 11. ما تبقّى (المهام المفتوحة)

1. ✅ ~~تنفيذ دليل الاختبار على الهاتف~~: كل الاختبارات الـ 15 نجحت.
2. ✅ ~~ضبط حقل اسم المستخدم~~: نُفّذ في الجلسة الثانية (`textVisiblePassword|textNoSuggestions`).
2b. ❓ **اقتراح معلّق:** خيار للمطوّر في القائمة يجعل السيرفر الوهمي يرفض التجديد، لعرض الخروج الإجباري على الهاتف. لم يُنفّذ بعد.
3. ❓ **موعد تسليم المهمة:** لم يذكره المستخدم بعد.
4. 🔄 **عند عودة FakeStore:** إعادة `fakestore.useMockBackend=false` واختبار التطبيق مع السيرفر الحقيقي.
5. 📚 **التحضير للمقابلة:**
    - دراسة `docs/Code_Explanation.pdf`، بدءاً من الجزء الثالث ثم التاسع.
    - التدرّب على العرض (القسم 55).
    - مراجعة أسئلة المقابلة في `Android_Test_Task_Guide.pdf` (القسم 11).

---

## 12. ما يُقال للمدير عن القرارات الخارجة عن نص المهمة

1. **سياسة فشل التجديد:** "عند رفض البيانات أُخرج المستخدم كما تطلب المهمة. أما عند انقطاع الشبكة أو تعطّل السيرفر فأُبقي الجلسة، لأن المشكلة مؤقتة وليست من المستخدم."
2. **السيرفر الوهمي:** "تعطّل FakeStore للجميع أثناء العمل، فأضفت سيرفراً وهمياً لنسخة التطوير فقط، يستبدل الطرف البعيد ويُبقي منطق الـ Token حقيقياً. نسخة الإنتاج تستخدم FakeStore دائماً."
3. **حفظ كلمة المرور:** "مشفّرة، وضرورية لمحاكاة التجديد لأن FakeStore لا يوفّر refresh token. في تطبيق حقيقي لا تُحفظ كلمة المرور."
4. **`EncryptedSharedPreferences`:** "مكتبة أوقفت Google تطويرها. وضعتها خلف interface اسمه `TokenStorage`، فيمكن استبدالها بـ DataStore + Keystore بتغيير سطر واحد."
