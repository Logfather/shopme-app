package de.shopme.data.input.speech

import de.shopme.domain.service.CatalogService

class SpeechCandidateResolver(
    private val catalogService: CatalogService
) {

    fun resolve(candidates: List<String>): String? {

        if (candidates.isEmpty()) return null

        var bestScore = 0
        var bestCandidate: String? = null

        candidates.forEach { phrase ->

            val words =
                phrase.lowercase()
                    .split(" ")
                    .filter { it.isNotBlank() }

            var score = 0

            words.forEach { word ->

                val match =
                    catalogService.resolveSpeech(word)

                if (match != null) {
                    score++
                }
            }

            if (score > bestScore) {
                bestScore = score
                bestCandidate = phrase
            }
        }

        return bestCandidate
    }
}