package com.example.autoelectricai.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VoiceToTextParser(private val context: Context) {

    private val _state = MutableStateFlow(VoiceToTextParserState())
    val state: StateFlow<VoiceToTextParserState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    fun startListening() {
        _state.update { it.copy(isSpeaking = true, error = null, spokenText = "") }
        
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                
                override fun onBeginningOfSpeech() {}
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    _state.update { it.copy(isSpeaking = false) }
                }
                
                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Ошибка аудио"
                        SpeechRecognizer.ERROR_CLIENT -> "Ошибка клиента"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Нет разрешения на микрофон"
                        SpeechRecognizer.ERROR_NETWORK -> "Ошибка сети"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Таймаут сети"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Не удалось распознать речь"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Служба занята"
                        SpeechRecognizer.ERROR_SERVER -> "Ошибка сервера"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Вы ничего не сказали"
                        else -> "Неизвестная ошибка ($error)"
                    }
                    _state.update { it.copy(isSpeaking = false, error = errorMessage) }
                }

                override fun onResults(results: Bundle?) {
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { resultList ->
                        if (resultList.isNotEmpty()) {
                            _state.update { it.copy(spokenText = resultList[0], isSpeaking = false) }
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { resultList ->
                        if (resultList.isNotEmpty()) {
                            _state.update { it.copy(spokenText = resultList[0]) }
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            startListening(intent)
        }
    }

    fun stopListening() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        _state.update { it.copy(isSpeaking = false) }
    }
    
    fun reset() {
        _state.update { VoiceToTextParserState() }
    }
}

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null
)
