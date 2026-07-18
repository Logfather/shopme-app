package de.shopme.testing.system.tools.knowledge.report

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.File
import java.io.FileReader

class CatalogServerKnowledgeMatchReporter(
    private val nearestCandidateLimit: Int = 5,
    private val queryExpander:
    NutritionRetrievalQueryExpander? =
        null
) {

    init {
        require(nearestCandidateLimit > 0) {
            "nearestCandidateLimit must be greater than zero."
        }
    }

    fun report(
        artifactFile: File,
        catalogKeys: Set<String>
    ): CatalogServerKnowledgeMatchReport {

        require(artifactFile.isFile) {
            "Server knowledge artifact does not exist: " +
                    artifactFile.absolutePath
        }

        val normalizedCatalogKeys =
            catalogKeys
                .asSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .toSortedSet()

        val retrievalQueriesByCatalogKey =
            normalizedCatalogKeys
                .associateWith { catalogKey ->

                    retrievalQueriesFor(
                        catalogKey =
                            catalogKey
                    )
                }

        val catalogKeysByRetrievalToken =
            buildCatalogTokenIndex(
                retrievalQueriesByCatalogKey =
                    retrievalQueriesByCatalogKey
            )

        val exactMatches =
            mutableSetOf<String>()

        val accumulators =
            normalizedCatalogKeys
                .associateWith {
                    NearestServerCandidateAccumulator(
                        limit =
                            nearestCandidateLimit
                    )
                }

        var serverKeyCount =
            0L

        JsonReader(
            FileReader(
                artifactFile
            )
        ).use { reader ->

            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {

                    "entries" ->
                        serverKeyCount +=
                            readEntries(
                                reader =
                                    reader,
                                catalogKeys =
                                    normalizedCatalogKeys,
                                retrievalQueriesByCatalogKey =
                                    retrievalQueriesByCatalogKey,
                                catalogKeysByRetrievalToken =
                                    catalogKeysByRetrievalToken,
                                exactMatches =
                                    exactMatches,
                                accumulators =
                                    accumulators
                            )

                    else ->
                        reader.skipValue()
                }
            }

            reader.endObject()
        }

        val unmatched =
            normalizedCatalogKeys
                .asSequence()
                .filterNot(
                    exactMatches::contains
                )
                .map { catalogKey ->

                    UnmatchedCatalogKnowledgeKey(
                        catalogKey =
                            catalogKey,
                        nearestCandidates =
                            accumulators
                                .getValue(
                                    catalogKey
                                )
                                .result()
                    )
                }
                .toList()

        return CatalogServerKnowledgeMatchReport(
            artifactName =
                artifactFile.name,
            catalogKeyCount =
                normalizedCatalogKeys.size,
            serverKeyCount =
                serverKeyCount,
            exactMatches =
                exactMatches.sorted(),
            unmatched =
                unmatched
        )
    }

    private fun readEntries(
        reader: JsonReader,
        catalogKeys: Set<String>,
        retrievalQueriesByCatalogKey:
        Map<String, List<String>>,
        catalogKeysByRetrievalToken:
        Map<String, Set<String>>,
        exactMatches: MutableSet<String>,
        accumulators:
        Map<String, NearestServerCandidateAccumulator>
    ): Long {

        require(
            reader.peek() ==
                    JsonToken.BEGIN_OBJECT
        ) {
            "Expected 'entries' to be a JSON object."
        }

        var serverKeyCount =
            0L

        reader.beginObject()

        while (reader.hasNext()) {

            val serverKey =
                reader
                    .nextName()
                    .trim()

            serverKeyCount++

            if (serverKey in catalogKeys) {

                exactMatches +=
                    serverKey

                reader.skipValue()

                continue
            }

            val affectedCatalogKeys =
                DiagnosticKnowledgeKeySimilarity
                    .tokenize(
                        value =
                            serverKey
                    )
                    .asSequence()
                    .flatMap { token ->

                        catalogKeysByRetrievalToken[
                            token
                        ]
                            .orEmpty()
                            .asSequence()
                    }
                    .toSortedSet()

            affectedCatalogKeys.forEach { catalogKey ->

                if (catalogKey in exactMatches) {
                    return@forEach
                }

                val retrievalQueries =
                    retrievalQueriesByCatalogKey
                        .getValue(
                            catalogKey
                        )

                val bestSimilarity =
                    bestSimilarity(
                        retrievalQueries =
                            retrievalQueries,
                        serverKey =
                            serverKey
                    )

                if (
                    bestSimilarity.score >
                    0.0
                ) {
                    accumulators
                        .getValue(
                            catalogKey
                        )
                        .offer(
                            NearestServerKnowledgeCandidate(
                                serverKey =
                                    serverKey,
                                score =
                                    bestSimilarity.score,
                                sharedTokens =
                                    bestSimilarity.sharedTokens
                            )
                        )
                }
            }

            /*
             * Nur der Server Key wird benötigt. Der möglicherweise
             * große Knowledge-Wert wird nicht materialisiert.
             */
            reader.skipValue()
        }

        reader.endObject()

        return serverKeyCount
    }

    private fun bestSimilarity(
        retrievalQueries: List<String>,
        serverKey: String
    ): DiagnosticKnowledgeKeyScore {

        require(retrievalQueries.isNotEmpty()) {
            "retrievalQueries must not be empty."
        }

        val originalQuery =
            retrievalQueries.first()

        var best =
            DiagnosticKnowledgeKeyScore(
                score =
                    0.0,
                sharedTokens =
                    emptyList()
            )

        retrievalQueries.forEach { retrievalQuery ->

            val candidate =
                DiagnosticKnowledgeKeySimilarity
                    .score(
                        catalogKey =
                            retrievalQuery,
                        serverKey =
                            serverKey
                    )

            val isOriginalQuery =
                retrievalQuery ==
                        originalQuery

            val accepted =
                isOriginalQuery ||
                        hasSufficientAliasEvidence(
                            retrievalQuery =
                                retrievalQuery,
                            similarity =
                                candidate
                        )

            if (
                accepted &&
                candidate.score >
                best.score
            ) {
                best =
                    candidate
            }
        }

        return best
    }

    private fun hasSufficientAliasEvidence(
        retrievalQuery: String,
        similarity: DiagnosticKnowledgeKeyScore
    ): Boolean {

        if (similarity.score <= 0.0) {
            return false
        }

        val aliasTokens =
            DiagnosticKnowledgeKeySimilarity
                .tokenize(
                    value =
                        retrievalQuery
                )

        val sharedTokens =
            similarity.sharedTokens
                .toSet()

        if (
            aliasTokens.isEmpty() ||
            sharedTokens.isEmpty()
        ) {
            return false
        }

        /*
         * Ein Einwort-Alias ist bereits eindeutig genug, sofern exakt
         * dieses Token im Server Key vorkommt.
         *
         * Beispiel:
         *
         * chervil -> kerbel
         */
        if (aliasTokens.size == 1) {
            return sharedTokens.containsAll(
                aliasTokens
            )
        }

        /*
         * Bei mehrteiligen Aliasen müssen mindestens zwei Tokens
         * übereinstimmen.
         *
         * Dadurch bleiben beispielsweise erhalten:
         *
         * meat loaf -> meat loaf
         * smoked spreadable sausage -> smoked sausage
         * chocolate caramel hazelnut candy ->
         * caramel chocolate candy
         */
        if (
            sharedTokens.size >=
            MIN_ALIAS_SHARED_TOKEN_COUNT
        ) {
            return true
        }

        /*
         * Ein einzelnes generisches Token darf keinen Candidate erzeugen.
         *
         * Beispiele:
         *
         * mace spice -> banana spice
         * gemeinsame Evidenz nur "spice"
         *
         * black salsify -> black
         * gemeinsame Evidenz nur "black"
         */
        val nonGenericSharedTokens =
            sharedTokens
                .minus(
                    GENERIC_ALIAS_TOKENS
                )

        if (nonGenericSharedTokens.isEmpty()) {
            return false
        }

        val aliasCoverage =
            sharedTokens.size.toDouble() /
                    aliasTokens.size.toDouble()

        return aliasCoverage >=
                MIN_ALIAS_TOKEN_COVERAGE
    }

    private fun retrievalQueriesFor(
        catalogKey: String
    ): List<String> {

        val expanded =
            queryExpander
                ?.expand(
                    catalogKey =
                        catalogKey
                )
                .orEmpty()

        val queries =
            if (expanded.isEmpty()) {
                listOf(
                    catalogKey
                )
            } else {
                expanded
            }

        val normalizedQueries =
            queries
                .asSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .toList()

        require(normalizedQueries.isNotEmpty()) {
            "No retrieval query available for catalog key: " +
                    catalogKey
        }

        return normalizedQueries
    }

    private fun buildCatalogTokenIndex(
        retrievalQueriesByCatalogKey:
        Map<String, List<String>>
    ): Map<String, Set<String>> {

        val mutableIndex =
            mutableMapOf<String, MutableSet<String>>()

        retrievalQueriesByCatalogKey
            .forEach { (catalogKey, retrievalQueries) ->

                retrievalQueries
                    .asSequence()
                    .flatMap { retrievalQuery ->

                        DiagnosticKnowledgeKeySimilarity
                            .tokenize(
                                value =
                                    retrievalQuery
                            )
                            .asSequence()
                    }
                    .distinct()
                    .forEach { token ->

                        mutableIndex
                            .getOrPut(
                                token
                            ) {
                                sortedSetOf()
                            }
                            .add(
                                catalogKey
                            )
                    }
            }

        return mutableIndex
            .mapValues { (_, values) ->

                values
                    .toSortedSet()
            }
            .toSortedMap()
    }

    private companion object {

        const val MIN_ALIAS_SHARED_TOKEN_COUNT =
            2

        const val MIN_ALIAS_TOKEN_COVERAGE =
            0.50

        val GENERIC_ALIAS_TOKENS =
            setOf(
                "black",
                "white",
                "red",
                "green",
                "yellow",
                "brown",
                "spice",
                "food",
                "product",
                "meal",
                "dish",
                "style",
                "original",
                "classic"
            )
    }
}