package de.shopme.tools.knowledge.ki_candidates

class CandidateCompatibilityPolicy {

    private companion object {

        val NON_WORD_REGEX =
            Regex("[^a-z0-9]+")
    }

    fun areCompatible(
        base: CanonicalKnowledgeCandidate,
        candidate: CanonicalKnowledgeCandidate
    ): Boolean {

        val baseFamily =
            base.foodFamilyKeys()

        val candidateFamily =
            candidate.foodFamilyKeys()

        if (baseFamily.isEmpty() || candidateFamily.isEmpty()) {
            return true
        }

        return baseFamily.intersect(candidateFamily).isNotEmpty()
    }



    private fun String.normalizedWords(): Set<String> {

        val words =
            linkedSetOf<String>()

        val builder =
            StringBuilder()

        fun flush() {
            if (builder.length >= 3) {
                words += builder.toString()
            }

            builder.clear()
        }

        for (char in lowercase()) {
            if (char.isLetterOrDigit()) {
                builder.append(char)
            } else {
                flush()

                if (words.size >= 8) {
                    return words
                }
            }
        }

        flush()

        return words
    }

    private fun CanonicalKnowledgeCandidate.foodFamilyKeys(): Set<String> {

        val values =
            buildSet {
                add(canonicalId)
                addAll(aliases)
            }

        return values
            .flatMap { value ->
                value.normalizedWords()
            }
            .filter { word ->
                word !in ignoredFamilyWords
            }
            .toSet()
    }

    private val ignoredFamilyWords =
        setOf(
            "with",
            "from",
            "pure",
            "fresh",
            "frozen",
            "organic",
            "natural",
            "style",
            "type",
            "raw",
            "cooked",
            "dried",
            "dehydrated",
            "premium",
            "classic",
            "original",
            "aromatic",
            "plant"
        )
}