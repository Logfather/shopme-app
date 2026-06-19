package de.shopme.data.input.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.domain.service.CatalogService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechController(
    private val context: Context,
    private val catalogService: CatalogService
) : DefaultLifecycleObserver {

    private val recognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private val _isListening =
        MutableStateFlow(false)

    private val emittedWords =
        mutableSetOf<String>()

    val isListening: StateFlow<Boolean> =
        _isListening.asStateFlow()

    private val _speechModeEnabled =
        MutableStateFlow(false)

    val speechModeEnabled =
        _speechModeEnabled.asStateFlow()

    private var resultListener:
            ((String) -> Unit)? = null

    fun setResultListener(
        listener: (String) -> Unit
    ) {
        resultListener = listener
    }

    private fun restartListening() {

        if (!_speechModeEnabled.value) {
            return
        }

        Handler(context.mainLooper)
            .postDelayed(
                {

                    try {

                        if (!_speechModeEnabled.value) {
                            return@postDelayed
                        }

                        recognizer.cancel()

                        startInternal()

                    } catch (e: Exception) {

                        RuntimeLog.speech(
                            "Restart failed",
                            e
                        )
                    }

                },
                500
            )
    }

    private fun emitNewWords(text: String) {

        val normalized =
            text.lowercase().trim()

        if (!emittedWords.contains(normalized)) {

            emittedWords.add(normalized)

            resultListener?.invoke(normalized)
        }
    }

    init {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {

            RuntimeLog.speech(
                "Speech recognition NOT available on device"
            )
        }

        recognizer.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {
                    // intentionally ignored
                }

                override fun onBeginningOfSpeech() {
                    // intentionally ignored
                }

                override fun onEndOfSpeech() {
                    // intentionally ignored
                }

                override fun onError(
                    error: Int
                ) {

                    RuntimeLog.runtime(
                        "Speech onError=$error"
                    )

                    RuntimeLog.speech(
                        "Speech error: $error"
                    )

                    when (error) {

                        SpeechRecognizer.ERROR_NO_MATCH -> {
                            if (_speechModeEnabled.value) {
                                restartListening()
                            }
                        }

                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                            if (_speechModeEnabled.value) {
                                restartListening()
                            }
                        }

                        SpeechRecognizer.ERROR_CLIENT -> {
                            if (_speechModeEnabled.value) {
                                restartListening()
                            }
                        }

                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {

                            RuntimeLog.speech(
                                "Permission error ignored for now"
                            )

                            if (_speechModeEnabled.value) {
                                restartListening()
                            }
                        }

                        else -> {

                            RuntimeLog.speech(
                                "Fatal speech error: $error"
                            )

                            stop()
                        }
                    }
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        ) ?: emptyList()

                    val bestCandidate =
                        resolveBestCandidate(matches)

                    val finalText =
                        bestCandidate
                            ?: matches.firstOrNull()

                    RuntimeLog.speech(
                        "Matches=$matches"
                    )

                    RuntimeLog.speech(
                        "BestCandidate=$bestCandidate"
                    )

                    RuntimeLog.speech(
                        "FinalText=$finalText"
                    )

                    finalText?.let { spoken ->

                        val normalized =
                            spoken
                                .lowercase()
                                .replace("bitte", "")
                                .replace("noch", "")
                                .replace("mal", "")
                                .trim()

                        emitNewWords(normalized)
                        RuntimeLog.speech(
                            "emitNewWords input=$normalized"
                        )
                    }

                    if (_speechModeEnabled.value) {
                        restartListening()
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {

                    //TODO: für spätere Liveanzeige nutzen
                    // intentionally ignored

                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )
    }

    fun start() {

        RuntimeLog.runtime(
            "Speech start requested"
        )

        emittedWords.clear()

        _speechModeEnabled.value = true

        if (_isListening.value) return

        _isListening.value = true

        RuntimeLog.runtime(
            "Listening=true"
        )

        startInternal()
    }

    private fun startInternal() {

        RuntimeLog.runtime(
            "Speech startInternal"
        )

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {

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

        RuntimeLog.runtime(
            "SpeechRecognizer.startListening"
        )

        RuntimeLog.speech(
            "Permission granted=${
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }"
        )

        recognizer.startListening(intent)
    }

    fun stop() {

        _speechModeEnabled.value = false

        RuntimeLog.runtime(
            "STOP CALLED"
        )

        _isListening.value = false

        RuntimeLog.runtime(
            "Listening=false"
        )

        recognizer.stopListening()
    }

    override fun onDestroy(
        owner: LifecycleOwner
    ) {

        recognizer.destroy()
    }

    private fun resolveBestCandidate(
        matches: List<String>
    ): String? {

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
                compoundNormalized
                    .split(" ")
                    .filter {
                        it.isNotBlank()
                    }

            var tokenScore = 0
            var phraseScore = 0

            tokens.forEach { token ->

                if (token.length < 2) {
                    return@forEach
                }

                val match =
                    catalogService.resolveSpeech(token)

                if (match != null) {
                    tokenScore++
                }
            }

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

    private fun splitGermanCompound(
        word: String
    ): String {

        val normalized =
            word.lowercase()

        if (normalized.length < 8) {
            return normalized
        }

        for (i in 4 until normalized.length - 3) {

            val left =
                normalized.substring(0, i)

            var right =
                normalized.substring(i)

            if (!catalogService.hasPrefix(left)) {
                continue
            }

            if (
                catalogService.normalize(right) != null
            ) {
                return "$left $right"
            }

            if (
                right.startsWith("s") &&
                right.length > 3
            ) {

                right =
                    right.substring(1)

                if (
                    catalogService.normalize(right) != null
                ) {
                    return "$left $right"
                }
            }

            if (
                right.endsWith("n") ||
                right.endsWith("e")
            ) {

                val stem =
                    right.dropLast(1)

                if (
                    catalogService.normalize(stem) != null
                ) {
                    return "$left $stem"
                }
            }
        }

        return normalized
    }
}