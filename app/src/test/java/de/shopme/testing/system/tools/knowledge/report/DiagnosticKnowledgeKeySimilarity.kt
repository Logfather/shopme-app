package de.shopme.testing.system.tools.knowledge.report

object DiagnosticKnowledgeKeySimilarity {

    fun score(
        catalogKey: String,
        serverKey: String
    ): DiagnosticKnowledgeKeyScore {

        val catalogTokens = tokenize(catalogKey)
        val serverTokens = tokenize(serverKey)

        if (catalogTokens.isEmpty() || serverTokens.isEmpty()) {
            return DiagnosticKnowledgeKeyScore(
                score = 0.0,
                sharedTokens = emptyList()
            )
        }

        val sharedTokens =
            catalogTokens
                .intersect(serverTokens)
                .sorted()

        if (sharedTokens.isEmpty()) {
            return DiagnosticKnowledgeKeyScore(
                score = 0.0,
                sharedTokens = emptyList()
            )
        }

        val intersectionSize = sharedTokens.size.toDouble()
        val unionSize = catalogTokens.union(serverTokens).size.toDouble()

        val jaccard =
            intersectionSize / unionSize

        val catalogCoverage =
            intersectionSize / catalogTokens.size.toDouble()

        val serverCoverage =
            intersectionSize / serverTokens.size.toDouble()

        /*
         * Catalog coverage is weighted more strongly because the diagnostic
         * question is:
         *
         * "Does the server key contain the meaning represented by the
         * catalog key?"
         *
         * This score is never used to accept or persist a match.
         */
        val weightedScore =
            catalogCoverage * 0.60 +
                    serverCoverage * 0.20 +
                    jaccard * 0.20

        return DiagnosticKnowledgeKeyScore(
            score = weightedScore.coerceIn(0.0, 1.0),
            sharedTokens = sharedTokens
        )
    }

    fun tokenize(value: String): Set<String> =
        value
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
            .split(Regex("\\s+"))
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .toSet()
}

data class DiagnosticKnowledgeKeyScore(
    val score: Double,
    val sharedTokens: List<String>
)