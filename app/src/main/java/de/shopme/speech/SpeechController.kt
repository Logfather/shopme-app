package de.shopme.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechController(
    private val context: Context,
    private val onTextRecognized: (String) -> Unit
) : DefaultLifecycleObserver {

    private val recognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> =
        _isListening.asStateFlow()

    init {
        recognizer.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()

                text?.let { onTextRecognized(it) }

                restartIfNeeded()
            }

            override fun onError(error: Int) {
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                    SpeechRecognizer.ERROR_NETWORK ->
                        restartIfNeeded()
                    else -> stop()
                }
            }

            override fun onEndOfSpeech() {}
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun start() {
        if (_isListening.value) return
        _isListening.value = true
        startInternal()
    }

    fun stop() {
        _isListening.value = false
        recognizer.stopListening()
    }

    private fun restartIfNeeded() {
        if (_isListening.value) {
            startInternal()
        }
    }

    private fun startInternal() {
        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.startListening(intent)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        recognizer.destroy()
    }
}