package de.shopme.testing.system.tools.knowledge.mapping.catalog

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequestGenerationReport
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequestWriter
import de.shopme.tools.knowledge.mapping.catalog.DefaultCatalogKnowledgeMatchRequestGenerator
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateCatalogKnowledgeMatchRequestsTest {

    @Test
    fun generateCatalogKnowledgeMatchRequests() {

        val projectRoot =
            File("..")

        val reportDirectory =
            File(
                projectRoot,
                "data/generated/reports/catalog-server-matches"
            )

        val outputDirectory =
            File(
                projectRoot,
                "data/generated/knowledge/match-requests"
            )

        require(reportDirectory.isDirectory) {
            "Catalog-server match report directory does not exist: " +
                    reportDirectory.absolutePath
        }

        val reportFiles =
            reportDirectory
                .listFiles()
                .orEmpty()
                .asSequence()
                .filter(File::isFile)
                .filter {
                    it.name.endsWith(
                        suffix = ".matches.json",
                        ignoreCase = true
                    )
                }
                .sortedBy {
                    it.name
                }
                .toList()

        assertTrue(
            reportFiles.isNotEmpty(),
            "No catalog-server match reports found"
        )

        val generator =
            DefaultCatalogKnowledgeMatchRequestGenerator()

        val writer =
            CatalogKnowledgeMatchRequestWriter()

        reportFiles.forEach { reportFile ->

            val sourceRoot =
                JsonParser
                    .parseString(
                        reportFile.readText()
                    )
                    .asJsonObject

            val artifactName =
                sourceRoot["artifactName"]
                    .asString

            val unmatchedCount =
                sourceRoot["unmatched"]
                    .asJsonArray
                    .size()

            val requests =
                generator.generate(
                    matchReportFile = reportFile
                )

            val outputFile =
                File(
                    outputDirectory,
                    artifactName
                        .removeSuffix(".json") +
                            ".match-requests.json"
                )

            writer.write(
                requests = requests,
                file = outputFile
            )

            verifyWrittenRequests(
                file = outputFile,
                expectedVersion = requests.version,
                expectedRequestCount =
                    requests.requests.size
            )

            CatalogKnowledgeMatchRequestGenerationReport(
                artifactName =
                    artifactName,
                unmatchedCount =
                    unmatchedCount,
                requestCount =
                    requests.requests.size,
                withoutCandidatesCount =
                    unmatchedCount -
                            requests.requests.size,
                outputFile =
                    outputFile.path
            ).printTo()
        }
    }


    private fun verifyWrittenRequests(
        file: File,
        expectedVersion: Int,
        expectedRequestCount: Int
    ) {

        assertTrue(
            file.isFile,
            "Match request file was not written: " +
                    file.absolutePath
        )

        val root =
            JsonParser
                .parseString(
                    file.readText()
                )
                .asJsonObject

        assertEquals(
            expectedVersion,
            root["version"].asInt
        )

        assertEquals(
            expectedRequestCount,
            root["requests"]
                .asJsonArray
                .size()
        )
    }
}