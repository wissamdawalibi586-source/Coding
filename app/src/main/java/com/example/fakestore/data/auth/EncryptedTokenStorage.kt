package com.example.fakestore.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [TokenStorage] backed by EncryptedSharedPreferences: keys and values are encrypted
 * with AES-256, and the master key lives in the Android Keystore, so it never leaves the device.
 *
 * read/save/clear are @Synchronized: a session is four separate keys, and without the
 * lock a reader could see a half-written session (e.g. the old token with the new tokenSavedAt).
 */
@Singleton
class EncryptedTokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) : TokenStorage {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    @Synchronized
    override fun read(): AuthSession? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val password = prefs.getString(KEY_PASSWORD, null) ?: return null
        val savedAt = prefs.getLong(KEY_SAVED_AT, 0L)
        return AuthSession(token, savedAt, Credentials(username, password))
    }

    @Synchronized
    override fun save(session: AuthSession) {
        // commit() writes synchronously; we are always on a background thread here.
        prefs.edit()
            .putString(KEY_TOKEN, session.token)
            .putLong(KEY_SAVED_AT, session.savedAtMillis)
            .putString(KEY_USERNAME, session.credentials.username)
            .putString(KEY_PASSWORD, session.credentials.password)
            .commit()
    }

    @Synchronized
    override fun clear() {
        prefs.edit().clear().commit()
    }

    private companion object {
        const val FILE_NAME = "secure_auth_prefs"
        const val KEY_TOKEN = "token"
        const val KEY_SAVED_AT = "token_saved_at"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
    }
}
