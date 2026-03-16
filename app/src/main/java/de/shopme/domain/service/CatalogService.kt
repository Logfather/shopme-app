package de.shopme.domain.service

import de.shopme.domain.catalog.CatalogIndex
import de.shopme.domain.catalog.CatalogItem
import de.shopme.domain.catalog.PhoneticEncoder
import de.shopme.domain.catalog.CatalogMatchScorer

class CatalogService(
    private val catalogIndex: CatalogIndex
) {

    private val phonetic = PhoneticEncoder()
    private val scorer = CatalogMatchScorer(phonetic)

    val index: CatalogIndex
        get() = catalogIndex

    fun normalize(word: String) =
        catalogIndex.normalize(word)

    fun autocomplete(prefix: String) =
        catalogIndex.autocomplete(prefix)

    fun matchSpeech(token: String) =
        catalogIndex.findByPhonetic(token)

    fun hasPrefix(prefix: String): Boolean =
        catalogIndex.hasPrefix(prefix)

    fun resolveSpeech(query: String): CatalogItem? {

        val q = query.lowercase().trim()

        // 1️⃣ Exact match
        catalogIndex.normalize(q)?.let {
            return it
        }

        // 2️⃣ Phonetic match
        val phoneticToken = phonetic.encode(q)

        catalogIndex.findByPhonetic(phoneticToken)?.let {
            return it
        }

        // 3️⃣ Prefix / autocomplete
        val candidates = catalogIndex.autocomplete(q)

        if (candidates.isEmpty()) {
            return null
        }

        val best = candidates.maxByOrNull {
            scorer.score(
                q,
                it.normalized
            )
        }

        // Sicherheitscheck gegen falsche Matches
        if (best != null && best.normalized.length > q.length + 5) {
            return null
        }

        return best
    }
}