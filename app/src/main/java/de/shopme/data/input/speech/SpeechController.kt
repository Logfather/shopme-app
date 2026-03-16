package de.shopme.data.input.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import de.shopme.domain.service.CatalogService

class SpeechController(
    private val context: Context,
    private val catalogService: CatalogService
) : DefaultLifecycleObserver {

    private val recognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private val _isListening = MutableStateFlow(false)

    private val emittedWords = mutableSetOf<String>()

    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var resultListener: ((String) -> Unit)? = null



    fun setResultListener(listener: (String) -> Unit) {
        resultListener = listener
    }

    private fun emitNewWords(text: String) {

        val normalized =
            text.lowercase()
                .replace(",", " ")
                .replace(" und ", " ")
                .trim()

        val catalogItem =
            catalogService.resolveSpeech(normalized)

        val result =
            catalogItem?.normalized ?: normalized

        if (!emittedWords.contains(result)) {

            emittedWords.add(result)

            resultListener?.invoke(result)
        }
    }

    init {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("SpeechController", "Speech recognition NOT available on device")
        }

        recognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                //("SpeechController", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                //("SpeechController", "Speech started")
            }

            override fun onEndOfSpeech() {
                //("SpeechController", "Speech ended")
            }

            override fun onError(error: Int) {

                Log.e("SpeechController", "Speech error: $error")

                when (error) {

                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {

                        _isListening.value = false
                    }

                    SpeechRecognizer.ERROR_CLIENT -> {
                        // ignorieren – passiert bei schnellen Restarts
                    }

                    else -> stop()
                }
            }



            private fun splitSpeechIntoCatalogWords(text: String): List<String> {

                val words = text.lowercase().split(" ")

                val result = mutableListOf<String>()

                words.forEach { word ->

                    // exakter Treffer
                    catalogService.resolveSpeech(word)?.let {
                        result.add(it.itemname)
                        return@forEach
                    }

                    // zusammengesetztes Wort zerlegen (z.B. wassereier)
                    for (i in 3 until word.length) {

                        val left = word.substring(0, i)
                        val right = word.substring(i)

                        val leftItem = catalogService.resolveSpeech(left)
                        val rightItem = catalogService.resolveSpeech(right)

                        if (leftItem != null && rightItem != null) {

                            result.add(leftItem.itemname)
                            result.add(rightItem.itemname)

                            return@forEach
                        }
                    }

                    result.add(word)
                }

                return result
            }

            override fun onResults(results: Bundle?) {

                val matches =
                    results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    ) ?: emptyList()

                val bestCandidate =
                    resolveBestCandidate(matches)

                val finalText =
                    bestCandidate ?: matches.firstOrNull()

                finalText?.let { spoken ->

                    val normalized =
                        spoken
                            .lowercase()
                            .replace("bitte", "")
                            .replace("noch", "")
                            .replace("mal", "")
                            .trim()

                    emitNewWords(normalized)
                }

                // ------------------------------------------------------------
                // dein vorhandener Restart-Code bleibt unverändert
                // ------------------------------------------------------------

                if (_isListening.value) {

                    Handler(context.mainLooper).postDelayed(
                        {
                            try {

                                recognizer.cancel()
                                startInternal()

                            } catch (e: Exception) {

                                Log.e("SpeechController","Restart failed", e)
                            }

                        },
                        500
                    )
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {

                val matches =
                    partialResults?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                val text = matches?.firstOrNull() ?: return

                emitNewWords(text)
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun start() {

        if (_isListening.value) return

        _isListening.value = true
        startInternal()
    }

    private fun startInternal() {

        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "de-DE"
                )

                putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    5
                )

                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    true
                )
            }

        recognizer.startListening(intent)
    }

    fun stop() {
        _isListening.value = false
        recognizer.stopListening()
    }

    private fun restartIfNeeded() {

        if (!_isListening.value) return

        Handler(context.mainLooper).postDelayed(
            {
                if (_isListening.value) {
                    start()
                }
            },
            500
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        recognizer.destroy()
    }

    private fun resolveBestCandidate(matches: List<String>): String? {

        var bestCandidate: String? = null
        var bestScore = -1

        matches.forEach { candidate ->

            val normalized =
                candidate
                    .lowercase()
                    .replace("bitte", "")
                    .replace("noch", "")
                    .replace("mal", "")
                    .replace("ein ", "")
                    .replace("eine ", "")
                    .replace("zweimal ", "2 ")
                    .replace("dreimal ", "3 ")
                    .trim()

            val compoundNormalized =
                splitGermanCompound(normalized)

            val tokens =
                compoundNormalized.split(" ")
                    .filter { it.isNotBlank() }

            var tokenScore = 0
            var phraseScore = 0

            // ------------------------------------------------
            // Token Score
            // ------------------------------------------------

            tokens.forEach { token ->

                if (token.length < 2) return@forEach

                val match =
                    catalogService.resolveSpeech(token)

                if (match != null) {
                    tokenScore++
                }
            }

            // ------------------------------------------------
            // Phrase Score
            // ------------------------------------------------

            if (tokens.size > 1) {

                val phrase =
                    tokens.joinToString(" ")

                val match =
                    catalogService.resolveSpeech(phrase)

                if (match != null) {
                    phraseScore += 2
                }
            }

            val totalScore =
                tokenScore + phraseScore

            if (totalScore > bestScore) {

                bestScore = totalScore
                bestCandidate = compoundNormalized
            }
        }

        return bestCandidate
    }

    private fun splitGermanCompound(word: String): String {

        val normalized = word.lowercase()

        if (normalized.length < 8) {
            return normalized
        }

        for (i in 4 until normalized.length - 3) {

            val left = normalized.substring(0, i)
            var right = normalized.substring(i)

            // schneller Prefix-Test über CatalogIndex
            if (!catalogService.hasPrefix(left)) {
                continue
            }

            // direktes Match
            if (catalogService.normalize(right) != null) {
                return "$left $right"
            }

            // deutsches Fugen-S entfernen
            if (right.startsWith("s") && right.length > 3) {

                right = right.substring(1)

                if (catalogService.normalize(right) != null) {
                    return "$left $right"
                }
            }

            // plural / einfache Flexion
            if (right.endsWith("n") || right.endsWith("e")) {

                val stem = right.dropLast(1)

                if (catalogService.normalize(stem) != null) {
                    return "$left $stem"
                }
            }
        }

        return normalized
    }


}