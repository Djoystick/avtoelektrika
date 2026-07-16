package com.example.autoelectricai.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val PREFERRED_AI = stringPreferencesKey("preferred_ai") // "gemini" or "openai"
        val GEMINI_PROXY_URL = stringPreferencesKey("gemini_proxy_url")

        // Embedded Default Keys from Jarvis
        const val DEFAULT_GEMINI_KEY = "AIzaSyCY82Ru5JfbdzI9XwIpnnq6Djmj5N9s9UA"
        const val DEFAULT_OPENAI_KEY = "sk-cvc-61b91d89d5d102529c0243449bad1c16887a9bfd10f9b05f5e34bc3ff5fc171c" // Dellmar
        const val DEFAULT_GEMINI_PROXY = "https://generativelanguage.googleapis.com/"
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { 
        val key = it[GEMINI_API_KEY]
        if (key.isNullOrBlank()) DEFAULT_GEMINI_KEY else key 
    }
    
    val openAiApiKey: Flow<String> = context.dataStore.data.map { 
        val key = it[OPENAI_API_KEY]
        if (key.isNullOrBlank()) DEFAULT_OPENAI_KEY else key 
    }
    
    val geminiProxyUrl: Flow<String> = context.dataStore.data.map {
        val url = it[GEMINI_PROXY_URL]
        if (url.isNullOrBlank()) DEFAULT_GEMINI_PROXY else url
    }
    
    val preferredAi: Flow<String> = context.dataStore.data.map { it[PREFERRED_AI] ?: "openai" }

    suspend fun saveGeminiKey(key: String) {
        context.dataStore.edit { it[GEMINI_API_KEY] = key }
    }

    suspend fun saveOpenAiKey(key: String) {
        context.dataStore.edit { it[OPENAI_API_KEY] = key }
    }

    suspend fun saveGeminiProxyUrl(url: String) {
        context.dataStore.edit { it[GEMINI_PROXY_URL] = url }
    }

    suspend fun savePreferredAi(provider: String) {
        context.dataStore.edit { it[PREFERRED_AI] = provider }
    }
}
