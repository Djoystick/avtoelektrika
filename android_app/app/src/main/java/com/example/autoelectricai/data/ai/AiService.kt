package com.example.autoelectricai.data.ai

import android.util.Log
import com.example.autoelectricai.utils.AppLogger
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import java.net.SocketTimeoutException

@Singleton
class AiService @Inject constructor(
    private val geminiApi: GeminiApiService,
    private val openAiApi: OpenAiApiService,
    @Named("pollinations") private val pollinationsApi: OpenAiApiService
) {
    companion object {
        private const val TAG = "AiService"
    }

    /**
     * Generates a step-by-step repair instruction using AI.
     * Tries preferred AI first, falls back to others, accumulating errors.
     */
    suspend fun generateDiagnosis(
        carBrand: String,
        carModel: String,
        carYear: String,
        system: String,
        symptoms: String,
        errorCodes: String,
        geminiKey: String,
        geminiProxyUrl: String,
        openAiKey: String,
        preferredAi: String = "gemini",
        catalogPlatforms: List<String> = emptyList(),
        catalogSystems: List<String> = emptyList()
    ): AiResult {
        val prompt = buildPrompt(carBrand, carModel, carYear, system, symptoms, errorCodes, catalogPlatforms, catalogSystems)
        val errors = mutableListOf<String>()

        suspend fun attempt(providerName: String, block: suspend () -> AiResult): AiResult? {
            return when (val result = block()) {
                is AiResult.Success -> result
                is AiResult.Error -> {
                    errors.add("[$providerName] ${result.message}")
                    null
                }
            }
        }

        if (preferredAi == "gemini" && geminiKey.isNotBlank()) {
            attempt("Gemini", { tryGemini(prompt, geminiKey, geminiProxyUrl) })?.let { return it }
            if (openAiKey.isNotBlank()) {
                attempt("Dellmar", { tryOpenAi(prompt, openAiKey) })?.let { return it }
            }
        } else if (openAiKey.isNotBlank()) {
            attempt("Dellmar", { tryOpenAi(prompt, openAiKey) })?.let { return it }
            if (geminiKey.isNotBlank()) {
                attempt("Gemini", { tryGemini(prompt, geminiKey, geminiProxyUrl) })?.let { return it }
            }
        } else {
            errors.add("[Конфигурация] API ключи не настроены.")
        }
        
        attempt("Pollinations", { tryPollinations(prompt) })?.let { return it }

        AppLogger.e(TAG, "All AI providers failed. Errors: $errors")
        return AiResult.Error("Не удалось получить ответ от ИИ.\nПричины:\n" + errors.joinToString("\n") + "\nПожалуйста, введите свой личный API-ключ в Настройках.")
    }

    private suspend fun tryGemini(prompt: String, apiKey: String, proxyUrl: String, model: String = "gemini-flash-latest"): AiResult {
        return try {
            val endpointUrl = if (proxyUrl.endsWith("/")) "${proxyUrl}v1beta/models/$model:generateContent" else "$proxyUrl/v1beta/models/$model:generateContent"
            val response = geminiApi.generate(
                url = endpointUrl,
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                )
            )
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                AiResult.Success(cleanJsonContent(text), "gemini")
            } else {
                AppLogger.w(TAG, "Gemini returned empty text or null candidates")
                AiResult.Error("Вернул пустой ответ.")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            AppLogger.e(TAG, "Gemini HTTP error ${e.code()}: $errorBody", e)
            if (e.code() == 503 && model == "gemini-flash-latest") {
                AppLogger.w(TAG, "Gemini $model is overloaded, falling back to gemini-flash-lite-latest")
                return tryGemini(prompt, apiKey, "gemini-flash-lite-latest")
            } else if (e.code() == 429) {
                return AiResult.Error("Исчерпан лимит бесплатных запросов (429).")
            }
            AiResult.Error("Ошибка HTTP ${e.code()}")
        } catch (e: SocketTimeoutException) {
            AiResult.Error("Превышено время ожидания ответа сервера (Тайм-аут).")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Gemini failed", e)
            AiResult.Error("Системная ошибка: ${e.message}")
        }
    }

    private suspend fun tryOpenAi(prompt: String, apiKey: String): AiResult {
        if (apiKey.isBlank()) return AiResult.Error("Пустой API ключ.")
        return try {
            val response = openAiApi.complete(
                authHeader = "Bearer $apiKey",
                request = OpenAiRequest(
                    messages = listOf(OpenAiMessage(role = "user", content = prompt))
                )
            )
            val text = response.choices?.firstOrNull()?.message?.content
            if (!text.isNullOrBlank()) {
                AiResult.Success(cleanJsonContent(text), "openai")
            } else {
                AppLogger.w(TAG, "OpenAI returned empty text")
                AiResult.Error("Вернул пустой ответ.")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            AppLogger.e(TAG, "OpenAI HTTP error ${e.code()}: $errorBody", e)
            if (e.code() == 429) return AiResult.Error("Исчерпан баланс или лимит квот (429).")
            AiResult.Error("Ошибка HTTP ${e.code()}")
        } catch (e: SocketTimeoutException) {
            AppLogger.e(TAG, "OpenAI timeout", e)
            AiResult.Error("Превышено время ожидания ответа сервера (Тайм-аут).")
        } catch (e: Exception) {
            AppLogger.e(TAG, "OpenAI failed", e)
            AiResult.Error("Системная ошибка: ${e.message}")
        }
    }
    
    private suspend fun tryPollinations(prompt: String): AiResult {
        return try {
            val response = pollinationsApi.complete(
                authHeader = "", // Pollinations doesn't strictly need it, but we pass empty
                request = OpenAiRequest(
                    messages = listOf(OpenAiMessage(role = "user", content = prompt)),
                    model = "openai" // Model format needed for Pollinations
                )
            )
            val text = response.choices?.firstOrNull()?.message?.content
            if (!text.isNullOrBlank()) {
                AiResult.Success(cleanJsonContent(text), "pollinations")
            } else {
                AppLogger.w(TAG, "Pollinations returned empty text")
                AiResult.Error("Вернул пустой ответ.")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            AppLogger.e(TAG, "Pollinations HTTP error ${e.code()}: $errorBody", e)
            AiResult.Error("Ошибка HTTP ${e.code()}")
        } catch (e: SocketTimeoutException) {
            AppLogger.e(TAG, "Pollinations timeout", e)
            AiResult.Error("Превышено время ожидания ответа сервера (Тайм-аут).")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Pollinations failed", e)
            AiResult.Error("Системная ошибка: ${e.message}")
        }
    }

    private fun buildPrompt(
        carBrand: String, carModel: String, carYear: String,
        system: String, symptoms: String, errorCodes: String,
        catalogPlatforms: List<String> = emptyList(),
        catalogSystems: List<String> = emptyList()
    ): String {
        val carInfo = buildString {
            if (carBrand.isNotBlank()) append("Марка: $carBrand")
            if (carModel.isNotBlank()) append(", Модель: $carModel")
            if (carYear.isNotBlank()) append(", Год: $carYear")
        }
        val errorInfo = if (errorCodes.isNotBlank()) "Коды ошибок OBD: $errorCodes\n" else ""

        val platformHint = if (catalogPlatforms.isNotEmpty()) {
            "\nДоступные платформы в каталоге для $carBrand:\n" +
            catalogPlatforms.joinToString("\n") { "  - \"${it}\"" } +
            "\nВыбери наиболее подходящую платформу из списка выше для поля encyclopediaPlatform."
        } else ""

        val systemHint = if (catalogSystems.isNotEmpty()) {
            "\nДоступные системы в каталоге:\n" +
            catalogSystems.joinToString("\n") { "  - \"${it}\"" } +
            "\nВыбери наиболее подходящую систему из списка выше для поля encyclopediaSystem."
        } else ""

        return """
Ты — профессиональный автоэлектрик с 20-летним опытом. Выступай в роли подробной энциклопедии по ремонту.

Автомобиль: $carInfo
Система: $system
Симптомы: $symptoms
$errorInfo$platformHint$systemHint

Сформируй ответ СТРОГО в формате JSON без markdown разметки (без ```json).
Структура должна быть такой:
{
  "encyclopediaBrand": "id бренда строчными буквами (например: toyota, vaz, hyundai)",
  "encyclopediaPlatform": "точное название платформы из списка выше (если список пуст — придумай подходящее)",
  "encyclopediaSystem": "точное название системы из списка выше (если список пуст — придумай подходящее)",
  "encyclopediaSubsystem": "наиболее конкретная подсистема (например: Коды ошибок, Распиновка, Live Data)",
  "solutions": [
    {
      "title": "Краткое название вероятной причины (например, 'Питание ЭБУ')",
      "description": "Энциклопедическое и развернутое описание того, как работает эта система, почему возникает поломка, какие могут быть последствия и физический смысл проблемы. Текст должен быть объемным и связным.",
      "steps": [
        "Шаг 1. Максимально подробное описание ПЕРВОГО атомарного действия. Указывайте нюансы, подводные камни, конкретные точки замера (номиналы, вольтаж, сопротивление), номера предохранителей, реле и пинов.",
        "Шаг 2. ВТОРОЕ атомарное действие. (Разбивайте процесс ремонта на множество мелких атомарных шагов. Не объединяйте разные действия в один пункт. Должно быть от 5 до 15 шагов для сложного ремонта.)",
        "Шаг 3. И так далее..."
      ]
    }
  ]
}

Требования:
- ОБЯЗАТЕЛЬНО заполни поля encyclopediaBrand, encyclopediaPlatform, encyclopediaSystem, encyclopediaSubsystem — они нужны для каталогизации решения!
- Шаги должны быть ОЧЕНЬ развернутыми, как в детальном пошаговом руководстве (Service Manual). Не пиши коротко!
- ВАЖНО: У тебя есть лимит в 4000 токенов. Обязательно уложись в него и убедись, что JSON полностью закрыт и синтаксически корректен. Если не влезаешь, сократи количество шагов, но сохрани целостность JSON.
- Каждый шаг — это одно конкретное атомарное действие (например, только снятие клеммы, или только замер на одном пине).
- Сортируй `solutions` от самых вероятных к редким.
- Отвечай только валидным JSON, никаких пояснений до или после.
        """.trimIndent()
    }
    fun cleanJsonContent(text: String): String {
        var clean = text.trim()
        if (clean.startsWith("```json", ignoreCase = true)) {
            clean = clean.substringAfter("```json").trim()
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```").trim()
        }
        if (clean.endsWith("```")) {
            clean = clean.substringBeforeLast("```").trim()
        }
        return clean
    }

    suspend fun verifySolution(
        carBrand: String,
        carModel: String,
        carYear: String,
        system: String,
        symptoms: String,
        errorCodes: String,
        solutionText: String,
        geminiKey: String,
        geminiProxyUrl: String,
        openAiKey: String,
        preferredAi: String = "gemini"
    ): AiVerificationResult? {
        val prompt = buildVerificationPrompt(carBrand, carModel, carYear, system, symptoms, errorCodes, solutionText)

        suspend fun attempt(block: suspend () -> AiResult): AiResult.Success? {
            return when (val result = block()) {
                is AiResult.Success -> result
                is AiResult.Error -> null
            }
        }

        val aiResult = if (preferredAi == "gemini" && geminiKey.isNotBlank()) {
            attempt { tryGemini(prompt, geminiKey, geminiProxyUrl, "gemini-flash-latest") } 
                ?: attempt { tryOpenAi(prompt, openAiKey) }
        } else if (openAiKey.isNotBlank()) {
            attempt { tryOpenAi(prompt, openAiKey) } 
                ?: attempt { tryGemini(prompt, geminiKey, geminiProxyUrl, "gemini-flash-latest") }
        } else null
        
        val finalResult = aiResult ?: attempt { tryPollinations(prompt) } ?: return null
        
        return parseVerificationResult(finalResult.jsonText)
    }

    private fun buildVerificationPrompt(
        carBrand: String, 
        carModel: String, 
        carYear: String, 
        system: String, 
        symptoms: String, 
        errorCodes: String, 
        solutionText: String
    ): String {
        val carInfo = buildString {
            if (carBrand.isNotBlank()) append("Марка: $carBrand")
            if (carModel.isNotBlank()) append(", Модель: $carModel")
            if (carYear.isNotBlank()) append(", Год: $carYear")
        }
        val sysInfo = if (system.isNotBlank()) "Система: $system" else ""

        return """
            Ты - главный эксперт-автоэлектрик. Твоя задача жестко и честно оценить достоверность предложенного решения от пользователя.
            
            Автомобиль: $carInfo
            $sysInfo
            Симптомы: $symptoms
            Коды ошибок: ${if (errorCodes.isNotBlank()) errorCodes else "Нет"}
            
            Предложенное решение: 
            $solutionText
            
            Оцени решение по следующим критериям:
            1. Применимость именно к этому автомобилю ($carInfo).
            2. Логичность связи между симптомами, ошибками и решением.
            3. Безопасность (нет ли опасного "колхоза", который может навредить авто).
            
            Если решение содержит опасные советы, не применимо к данной машине или нелогично — смело ставь низкий балл.
            Если решение точное, безопасное и профессиональное — ставь высокий балл.
            
            Верни СТРОГО JSON формат (без разметки Markdown):
            {
              "score": 85,
              "reason": "Краткое обоснование оценки на русском языке (почему снят балл или почему это отличное решение)."
            }
            "score" должно быть целым числом от 0 до 100.
        """.trimIndent()
    }

    private fun parseVerificationResult(jsonText: String): AiVerificationResult? {
        return try {
            val cleanJson = cleanJsonContent(jsonText)
            val gson = com.google.gson.GsonBuilder().setLenient().create()
            gson.fromJson(cleanJson, AiVerificationResult::class.java)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to parse verification result", e)
            null
        }
    }
}

data class AiVerificationResult(val score: Int, val reason: String)

sealed class AiResult {
    data class Success(val jsonText: String, val provider: String) : AiResult()
    data class Error(val message: String) : AiResult()
}

