package com.example.autoelectricai.data.ai

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// ====== Gemini API ======
interface GeminiApiService {
    @POST
    suspend fun generate(
        @retrofit2.http.Url url: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

// ====== OpenAI API ======
interface OpenAiApiService {
    @POST("chat/completions")
    suspend fun complete(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}

data class OpenAiRequest(
    val model: String = "claude-sonnet-4.6",
    val messages: List<OpenAiMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 4000
)

data class OpenAiMessage(
    val role: String,
    val content: String
)

data class OpenAiResponse(
    val choices: List<OpenAiChoice>?
)

data class OpenAiChoice(
    val message: OpenAiMessage?
)

// ====== Parsed Response Models ======
data class DiagnosisResponse(
    @SerializedName("solutions") val solutions: List<SolutionBlock>
)

data class SolutionBlock(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("steps") val steps: List<String>
)
