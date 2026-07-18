package de.shopme.testing.system.tools.knowledge.mapping.catalog

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.openai.OpenAIProvider
import de.shopme.tools.knowledge.ai.openai.OpenAIProviderConfig
import de.shopme.tools.knowledge.ai.openai.RealOpenAIHttpClient
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisions
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequestContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequests
import de.shopme.tools.knowledge.mapping.catalog.OpenAICatalogKnowledgeMatcherFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunOpenAICatalogKnowledgeMatchSampleTest {

    @Test
    fun matchCatalogKnowledgeSampleWithOpenAI() {

        val environment =
            System.getenv()

        requireRealTestEnabled(
            environment = environment
        )

        val projectRoot =
            File("..")

        val requestFile =
            File(
                projectRoot,
                "data/generated/knowledge/match-requests/" +
                        "nutrition.match-requests.json"
            )

        require(requestFile.isFile) {
            "Catalog knowledge match request file does not exist: " +
                    requestFile.absolutePath
        }

        val allRequests =
            readRequests(
                file = requestFile
            )

        assertTrue(
            allRequests.requests.size >= SAMPLE_SIZE,
            "Expected at least $SAMPLE_SIZE match requests, " +
                    "but found ${allRequests.requests.size}"
        )

        val sampleRequests =
            selectEvenlyDistributedSample(
                requests = allRequests.requests,
                sampleSize = SAMPLE_SIZE
            )

        val openAIConfig =
            OpenAIProviderConfig
                .fromEnvironment()

        val providerConfig =
            AIProviderConfig(
                providerName = "openai",
                model = openAIConfig.model,
                apiKey = openAIConfig.apiKey,
                endpoint = openAIConfig.endpoint,

                /*
                 * GPT-5.5 accepts only its default temperature.
                 */
                temperature = 1.0
            )

        val openAIProvider =
            OpenAIProvider(
                config = providerConfig,
                httpClient =
                    RealOpenAIHttpClient(
                        config = providerConfig
                    )
            )

        val matcher =
            OpenAICatalogKnowledgeMatcherFactory(
                openAIProvider = openAIProvider
            ).create()

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OPENAI CATALOG KNOWLEDGE MATCH SAMPLE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Model             : ${providerConfig.model}")
        println("Source file       : ${requestFile.path}")
        println("Available requests: ${allRequests.requests.size}")
        println("Sample size       : ${sampleRequests.size}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        val decisions =
            sampleRequests
                .mapIndexed { index, request ->

                    println()
                    println(
                        "[${index + 1}/${sampleRequests.size}] " +
                                request.catalogKey
                    )

                    println(
                        "Candidates: " +
                                request.candidates.size
                    )

                    request.candidates
                        .forEachIndexed { candidateIndex, candidate ->

                            println(
                                "  ${candidateIndex + 1}. " +
                                        candidate.serverKey +
                                        " (score=" +
                                        candidate.diagnosticScore +
                                        ")"
                            )
                        }

                    val decision =
                        matcher.match(
                            request = request
                        )

                    verifyDecision(
                        request = request,
                        decision = decision
                    )

                    println(
                        "Decision   : ${decision.type}"
                    )

                    println(
                        "Selected   : " +
                                (decision.selectedServerKey ?: "-")
                    )

                    println(
                        "Confidence : ${decision.confidence}"
                    )

                    println(
                        "Reason     : ${decision.reason}"
                    )

                    decision
                }
                .sortedWith(
                    CatalogKnowledgeMatchDecisions.DECISION_ORDER
                )

        val decisionBatch =
            CatalogKnowledgeMatchDecisions(
                version =
                    CatalogKnowledgeMatchDecisionContract.CURRENT_VERSION,
                decisions = decisions
            )

        val outputFile =
            File(
                projectRoot,
                "data/generated/knowledge/match-decisions/samples/" +
                        "nutrition.sample.match-decisions.json"
            )

        writeDecisions(
            decisions = decisionBatch,
            file = outputFile
        )

        printSummary(
            decisions = decisionBatch.decisions,
            outputFile = outputFile
        )

        assertEquals(
            SAMPLE_SIZE,
            decisionBatch.decisions.size
        )
    }


    private fun requireRealTestEnabled(
        environment: Map<String, String>
    ) {

        val enabled =
            environment[RUN_TEST_ENVIRONMENT_VARIABLE]
                ?.trim()
                ?.equals(
                    other = "true",
                    ignoreCase = true
                )
                ?: false

        require(enabled) {
            "Real OpenAI sample test is disabled. " +
                    "Set $RUN_TEST_ENVIRONMENT_VARIABLE=true."
        }
    }


    private fun readRequests(
        file: File
    ): CatalogKnowledgeMatchRequests {

        val root =
            JsonParser
                .parseString(
                    file.readText()
                )
                .asJsonObject

        val version =
            root.requiredInt(
                key = "version"
            )

        require(
            version ==
                    CatalogKnowledgeMatchRequestContract.CURRENT_VERSION
        ) {
            "Unsupported catalog knowledge match request version: " +
                    version
        }

        val requests =
            root
                .requiredArray(
                    key = "requests"
                )
                .map { requestElement ->

                    val requestObject =
                        requestElement.asJsonObject

                    val candidates =
                        requestObject
                            .requiredArray(
                                key = "candidates"
                            )
                            .map { candidateElement ->

                                val candidateObject =
                                    candidateElement.asJsonObject

                                CatalogKnowledgeMatchCandidate(
                                    serverKey =
                                        candidateObject.requiredString(
                                            key = "serverKey"
                                        ),
                                    diagnosticScore =
                                        candidateObject.requiredDouble(
                                            key = "diagnosticScore"
                                        ),
                                    sharedTokens =
                                        candidateObject
                                            .requiredArray(
                                                key = "sharedTokens"
                                            )
                                            .map { tokenElement ->
                                                tokenElement
                                                    .asString
                                                    .trim()
                                            }
                                )
                            }

                    CatalogKnowledgeMatchRequest(
                        catalogKey =
                            requestObject.requiredString(
                                key = "catalogKey"
                            ),
                        serverArtifact =
                            requestObject.requiredString(
                                key = "serverArtifact"
                            ),
                        candidates =
                            candidates
                    )
                }

        return CatalogKnowledgeMatchRequests(
            version = version,
            requests = requests
        )
    }


    private fun selectEvenlyDistributedSample(
        requests: List<CatalogKnowledgeMatchRequest>,
        sampleSize: Int
    ): List<CatalogKnowledgeMatchRequest> {

        require(sampleSize > 0) {
            "sampleSize must be greater than zero"
        }

        require(requests.size >= sampleSize) {
            "Not enough requests for sample size $sampleSize"
        }

        if (sampleSize == 1) {
            return listOf(
                requests[requests.lastIndex / 2]
            )
        }

        val selectedIndices =
            (0 until sampleSize)
                .map { sampleIndex ->

                    val ratio =
                        sampleIndex.toDouble() /
                                (sampleSize - 1).toDouble()

                    (ratio * requests.lastIndex)
                        .toInt()
                }
                .distinct()

        require(selectedIndices.size == sampleSize) {
            "Could not select $sampleSize unique sample indices"
        }

        return selectedIndices.map(requests::get)
    }


    private fun verifyDecision(
        request: CatalogKnowledgeMatchRequest,
        decision: CatalogKnowledgeMatchDecision
    ) {

        assertEquals(
            request.catalogKey,
            decision.catalogKey,
            "Decision catalogKey differs from request"
        )

        assertEquals(
            request.serverArtifact,
            decision.serverArtifact,
            "Decision serverArtifact differs from request"
        )

        assertTrue(
            decision.confidence in 0.0..1.0,
            "Decision confidence must be between 0 and 1"
        )

        assertTrue(
            decision.reason.isNotBlank(),
            "Decision reason must not be blank"
        )

        when (decision.type) {

            CatalogKnowledgeMatchDecisionType.MATCH -> {

                val selectedServerKey =
                    requireNotNull(
                        decision.selectedServerKey
                    ) {
                        "MATCH decision has no selectedServerKey"
                    }

                assertTrue(
                    request.candidates.any { candidate ->
                        candidate.serverKey ==
                                selectedServerKey
                    },
                    "OpenAI selected a server key that was not " +
                            "part of the request: $selectedServerKey"
                )
            }

            CatalogKnowledgeMatchDecisionType.NO_MATCH -> {

                assertEquals(
                    null,
                    decision.selectedServerKey,
                    "NO_MATCH decision must not select a server key"
                )
            }
        }
    }


    private fun writeDecisions(
        decisions: CatalogKnowledgeMatchDecisions,
        file: File
    ) {

        val parentDirectory =
            file.parentFile

        if (
            parentDirectory != null &&
            !parentDirectory.exists()
        ) {
            check(
                parentDirectory.mkdirs()
            ) {
                "Could not create decision output directory: " +
                        parentDirectory.absolutePath
            }
        }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        file.writeText(
            gson.toJson(decisions)
        )
    }


    private fun printSummary(
        decisions: List<CatalogKnowledgeMatchDecision>,
        outputFile: File
    ) {

        val matchCount =
            decisions.count {
                it.type ==
                        CatalogKnowledgeMatchDecisionType.MATCH
            }

        val noMatchCount =
            decisions.count {
                it.type ==
                        CatalogKnowledgeMatchDecisionType.NO_MATCH
            }

        val averageConfidence =
            decisions
                .map {
                    it.confidence
                }
                .average()

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OPENAI MATCH SAMPLE SUMMARY")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Decisions          : ${decisions.size}")
        println("MATCH              : $matchCount")
        println("NO_MATCH           : $noMatchCount")
        println(
            "Average confidence : " +
                    "%.3f".format(averageConfidence)
        )
        println("Written            : ${outputFile.path}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
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
            "Missing or blank string '$key'"
        }

        return value
    }


    private fun JsonObject.requiredInt(
        key: String
    ): Int {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing integer '$key'"
        }

        return element.asInt
    }


    private fun JsonObject.requiredDouble(
        key: String
    ): Double {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing number '$key'"
        }

        return element.asDouble
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


    companion object {

        private const val RUN_TEST_ENVIRONMENT_VARIABLE =
            "RUN_OPENAI_CATALOG_MATCH_SAMPLE_TEST"

        private const val SAMPLE_SIZE =
            10
    }
}