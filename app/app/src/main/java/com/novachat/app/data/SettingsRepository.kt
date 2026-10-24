package com.novachat.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persists [Settings] in EncryptedSharedPreferences (AES256-GCM values, AES256-SIV keys).
 * Falls back to private preferences only if the keystore is unavailable on the device.
 */
class SettingsRepository(context: Context) {

    private val appContext = context.applicationContext

    var isEncrypted: Boolean = true
        private set

    private val prefs: SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            FILE_ENCRYPTED,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse { error ->
        Log.w(TAG, "Encrypted preferences unavailable, using private prefs", error)
        isEncrypted = false
        appContext.getSharedPreferences(FILE_PLAIN, Context.MODE_PRIVATE)
    }

    fun load(): Settings {
        val provider = Provider.fromId(prefs.getString(KEY_PROVIDER, Provider.DEEPSEEK.id)!!)
        return Settings(
            providerId = provider.id,
            baseUrl = prefs.getString(KEY_BASE_URL, provider.baseUrl) ?: provider.baseUrl,
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            model = prefs.getString(KEY_MODEL, provider.model) ?: provider.model,
            systemPrompt = prefs.getString(KEY_SYSTEM, Settings.DEFAULT_SYSTEM_PROMPT)
                ?: Settings.DEFAULT_SYSTEM_PROMPT,
            temperature = prefs.getFloat(KEY_TEMPERATURE, 0.7f)
        )
    }

    fun save(settings: Settings) {
        prefs.edit()
            .putString(KEY_PROVIDER, settings.providerId)
            .putString(KEY_BASE_URL, settings.baseUrl)
            .putString(KEY_API_KEY, settings.apiKey)
            .putString(KEY_MODEL, settings.model)
            .putString(KEY_SYSTEM, settings.systemPrompt)
            .putFloat(KEY_TEMPERATURE, settings.temperature)
            .apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    private companion object {
        const val TAG = "SettingsRepository"
        const val FILE_ENCRYPTED = "nova_secure_prefs"
        const val FILE_PLAIN = "nova_prefs_fallback"
        const val KEY_PROVIDER = "provider"
        const val KEY_BASE_URL = "base_url"
        const val KEY_API_KEY = "api_key"
        const val KEY_MODEL = "model"
        const val KEY_SYSTEM = "system_prompt"
        const val KEY_TEMPERATURE = "temperature"
    }
}
