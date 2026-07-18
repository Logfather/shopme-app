package de.shopme.tools.knowledge.mapping.catalog

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class DefaultCatalogKnowledgeMatchRequestGenerator :
    CatalogKnowledgeMatchRequestGenerator {

    override fun generate(
        matchReportFile: File
    ): CatalogKnowledgeMatchRequests {

        require(matchReportFile.isFile) {
            "Catalog-server match report does not exist: " +
                    matchReportFile.absolutePath
        }

        val root =
            JsonParser
                .parseString(
                    matchReportFile.readText()
                )
                .asJsonObject

        val artifactName =
            root.requiredString(
                key = "artifactName"
            )

        val requests =
            root
                .requiredArray(
                    key = "unmatched"
                )
                .asSequence()
                .map { element ->
                    element.asJsonObject
                }
                .mapNotNull { unmatched ->
                    unmatched.toRequestOrNull(
                        artifactName = artifactName
                    )
                }
                .sortedWith(
                    CatalogKnowledgeMatchRequests.REQUEST_ORDER
                )
                .toList()

        return CatalogKnowledgeMatchRequests(
            version =
                CatalogKnowledgeMatchRequestContract.CURRENT_VERSION,
            requests = requests
        )
    }


    private fun JsonObject.toRequestOrNull(
        artifactName: String
    ): CatalogKnowledgeMatchRequest? {

        val candidates =
            requiredArray(
                key = "nearestCandidates"
            )
                .asSequence()
                .map { element ->
                    element.asJsonObject
                }
                .map { candidate ->
                    candidate.toMatchCandidate()
                }
                .sortedWith(
                    CatalogKnowledgeMatchRequest.CANDIDATE_ORDER
                )
                .toList()

        if (candidates.isEmpty()) {
            return null
        }

        return CatalogKnowledgeMatchRequest(
            catalogKey =
                requiredString(
                    key = "catalogKey"
                ),
            serverArtifact =
                artifactName,
            candidates =
                candidates
        )
    }


    private fun JsonObject.toMatchCandidate():
            CatalogKnowledgeMatchCandidate {

        val sharedTokens =
            requiredArray(
                key = "sharedTokens"
            )
                .asSequence()
                .map { element ->
                    element.asString.trim()
                }
                .filter(String::isNotBlank)
                .distinct()
                .sorted()
                .toList()

        return CatalogKnowledgeMatchCandidate(
            serverKey =
                requiredString(
                    key = "serverKey"
                ),
            diagnosticScore =
                requiredDouble(
                    key = "score"
                ),
            sharedTokens =
                sharedTokens
        )
    }


    private fun JsonObject.requiredString(
        key: String
    ): String {

        val value =
            get(key)
                ?.takeIf {
                    !it.isJsonNull &&
                            it.isJsonPrimitive
                }
                ?.asString
                ?.trim()

        require(
            !value.isNullOrBlank()
        ) {
            "Missing or blank '$key'"
        }

        return value
    }


    private fun JsonObject.requiredDouble(
        key: String
    ): Double {

        val value =
            get(key)
                ?.takeIf {
                    !it.isJsonNull &&
                            it.isJsonPrimitive
                }
                ?.asDouble

        require(value != null) {
            "Missing numeric '$key'"
        }

        return value
    }


    private fun JsonObject.requiredArray(
        key: String
    ) =
        get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
            ?: error(
                "Missing JSON array '$key'"
            )
}