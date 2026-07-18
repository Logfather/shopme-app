package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.mapping.catalog.DefaultCatalogKnowledgeMatchRequestGenerator
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogKnowledgeMatchRequestGeneratorTest {

    @Test
    fun generatesRequestsOnlyForUnmatchedKeysWithCandidates() {

        val reportFile =
            createTempFile(
                prefix = "catalog-server-match",
                suffix = ".json"
            ).toFile()

        reportFile.writeText(
            """
            {
              "artifactName": "environmental_impact.json",
              "catalogKeyCount": 3,
              "serverKeyCount": 3,
              "exactMatches": [
                "apple"
              ],
              "unmatched": [
                {
                  "catalogKey": "semi skimmed milk",
                  "nearestCandidates": [
                    {
                      "serverKey": "milk semi skimmed uht",
                      "score": 0.81,
                      "sharedTokens": [
                        "milk",
                        "semi",
                        "skimmed"
                      ]
                    },
                    {
                      "serverKey": "milk semi skimmed pasteurized",
                      "score": 0.90,
                      "sharedTokens": [
                        "milk",
                        "semi",
                        "skimmed"
                      ]
                    }
                  ]
                },
                {
                  "catalogKey": "elderflower syrup",
                  "nearestCandidates": []
                }
              ]
            }
            """.trimIndent()
        )

        val result =
            DefaultCatalogKnowledgeMatchRequestGenerator()
                .generate(
                    matchReportFile = reportFile
                )

        assertEquals(
            1,
            result.version
        )

        assertEquals(
            1,
            result.requests.size
        )

        val request =
            result.requests.single()

        assertEquals(
            "semi skimmed milk",
            request.catalogKey
        )

        assertEquals(
            "environmental_impact.json",
            request.serverArtifact
        )

        assertEquals(
            listOf(
                "milk semi skimmed pasteurized",
                "milk semi skimmed uht"
            ),
            request.candidates.map {
                it.serverKey
            }
        )

        assertEquals(
            listOf(
                0.90,
                0.81
            ),
            request.candidates.map {
                it.diagnosticScore
            }
        )
    }
}