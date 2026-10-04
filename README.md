# FakeStore Auth — Android Test Task

A small Android app that logs in against the [FakeStore API](https://fakestoreapi.com),
stores the token securely, and consumes "protected" product endpoints with automatic
token expiry, refresh, and concurrent-request handling.

**Stack:** Kotlin · MVVM · Retrofit + OkHttp · Hilt · Coroutines/Flow · Navigation Component ·
RecyclerView + ViewBinding · EncryptedSharedPreferences · Coil

Test account: `mor_2314` / `83r5^_`

---

## Requirements checklist

| Requirement | Where |
|---|---|
| Login screen with loading & error states | `ui/login/LoginFragment`, `LoginViewModel` |
| Token stored securely | `data/auth/EncryptedTokenStorage` (AES-256, key in Android Keystore) |
| Authorization interceptor (`Bearer <token>`) | `data/auth/AuthInterceptor` |
| Token valid for 60 s, `tokenSavedAt` stored | `AuthManager.TOKEN_LIFETIME_MS`, `AuthSession.savedAtMillis` |
| Never send an expired token; refresh automatically | `AuthManager.getValidToken()` (called before every request) |
| Concurrent requests → one refresh, others wait | `AuthManager` double-checked locking on `refreshLock` |
| Refresh simulated by re-calling login | `AuthManager.refresh()` → `AuthApi.login()` |
| No infinite retry loops | separate OkHttp client for login; one refresh per request; `TokenAuthenticator` retries once |
| Refresh fails → force logout | `AuthManager.forceLogout()` → `AuthEvent.SessionExpired` → `MainActivity` |
| Products list (RecyclerView) + details | `ui/products`, `ui/detail` |
| Logout clears token and returns to Login | `ProductsViewModel.logout()` + `action_global_login` (clears back stack) |
| MVVM, Retrofit + OkHttp | whole project |

## Architecture

```
UI (Fragments + ViewModels)
   │  StateFlow<UiState>
   ▼
ProductRepository ──► ProductApi ──► [AuthClient: AuthInterceptor → Logging] ──► server
                                          │ getValidToken()
                                          ▼
                                     AuthManager ──► TokenStorage (encrypted)
                                          │ refresh = login again
                                          ▼
                                     AuthApi ──► [PlainClient: Logging only] ──► server
```

* **Two OkHttp clients.** `AuthApi` uses a client *without* `AuthInterceptor`; otherwise the
  login call made during a refresh would itself require a valid token → infinite recursion.
* **Proactive expiry check.** FakeStore never returns 401 for products, so expiry is checked
  *before* sending. `TokenAuthenticator` is an extra safety net for real backends (401 →
  refresh → retry once → otherwise force logout).
* **Concurrency.** `getValidToken()` has a lock-free fast path for valid tokens. When the token
  is expired, callers enter `synchronized(refreshLock)`; the first one refreshes, the rest
  re-check after acquiring the lock, see the new token, and continue without another login.
  A plain lock (not a coroutine `Mutex`) is used because OkHttp interceptors are blocking.
* **Exceptions thrown from the interceptor extend `IOException`.** OkHttp only propagates
  `IOException`s as call failures; anything else would crash the dispatcher thread.

## Design decisions

* **Refresh failure policy.** If the server *rejects* the credentials (HTTP 4xx) the session is
  wiped and the user is sent to Login. If the refresh fails because of the *network*, the
  session is kept and the request fails with a "no internet" error, so a flaky connection
  does not log the user out. Each request attempts at most one refresh, so there is no loop.
* **Stored credentials.** Simulating refresh requires re-sending the username and password, so
  they are stored encrypted next to the token. A production app would store a server-issued
  refresh token instead and never persist the password.
* **Logout during a refresh.** A generation counter prevents a refresh that started before
  logout from writing the session back afterwards.
* **Secure storage.** `EncryptedSharedPreferences` is used behind a `TokenStorage` interface.
  The library is deprecated by Google; it can be replaced with DataStore + a Keystore-backed
  cipher (e.g. Tink) without touching any other class. `allowBackup="false"` keeps the token
  out of backups.
* **Logging.** `HttpLoggingInterceptor` at `BASIC` level in debug builds only, with the
  `Authorization` header redacted; bodies (which contain the password) are never logged.

## Running

1. Open the project in Android Studio (Koala or newer) and let Gradle sync.
2. Run the `app` configuration on an emulator or device (API 24+).
3. Unit tests: `./gradlew test`

## Demonstrating token handling

Filter Logcat by `okhttp.OkHttpClient`:

1. Log in → one `POST /auth/login`.
2. Open products / a product within 60 s → only `GET /products…`, no login.
3. Wait more than 60 s, then use **⋮ → Run concurrency test** (5 parallel requests) →
   exactly **one** `POST /auth/login`, followed by the 5 product requests.
4. **⋮ → Log out** → back on Login; pressing Back exits the app.

## Tests

`app/src/test/.../data/auth` — JVM tests using `MockWebServer`, a `FakeClock` and in-memory storage:

* `AuthManagerTest` — expiry at exactly 60 s, clock going backwards, refresh, **10 threads → 1 login**,
  rejected refresh → logout + event, network failure keeps the session, logout during refresh.
* `AuthInterceptorTest` — every request carries `Bearer`, expired token refreshed *before*
  sending, **5 parallel Retrofit calls → 1 login**, no session → request never sent.
* `TokenAuthenticatorTest` — server keeps returning 401 → exactly one retry, then logout.
