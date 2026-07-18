package de.shopme.testing.system.tools.knowledge.report

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogServerKnowledgeMatchReporterTest {

    @Test
    fun reportsExactAndNearestServerKeys() {

        val directory =
            createTempDirectory(
                prefix = "catalog-server-report"
            ).toFile()

        val artifactFile =
            File(
                directory,
                "environmental_impact.json"
            )

        artifactFile.writeText(
            """
            {
              "version": 1,
              "entries": {
                "apple": {
                  "value": 1
                },
                "milk semi skimmed uht": {
                  "value": 2
                },
                "beef steak grilled": {
                  "value": 3
                }
              }
            }
            """.trimIndent()
        )

        val report =
            CatalogServerKnowledgeMatchReporter(
                nearestCandidateLimit = 3
            ).report(
                artifactFile = artifactFile,
                catalogKeys = setOf(
                    "apple",
                    "semi skimmed milk",
                    "elderflower syrup"
                )
            )

        assertEquals(
            3,
            report.catalogKeyCount
        )

        assertEquals(
            3L,
            report.serverKeyCount
        )

        assertEquals(
            listOf("apple"),
            report.exactMatches
        )

        val milk =
            report.unmatched.single {
                it.catalogKey == "semi skimmed milk"
            }

        assertEquals(
            "milk semi skimmed uht",
            milk.nearestCandidates.first().serverKey
        )

        val elderflower =
            report.unmatched.single {
                it.catalogKey == "elderflower syrup"
            }

        assertTrue(
            elderflower.nearestCandidates.isEmpty()
        )
    }

    @Test
    fun retrievesCandidatesThroughNutritionAliases() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-alias-report"
            )
                .toFile()

        try {
            val artifactFile =
                File(
                    directory,
                    "nutrition.json"
                )

            artifactFile.writeText(
                """
            {
              "version": 1,
              "entries": {
                "kerbel raw": {
                  "value": 1
                },
                "black salsify cooked": {
                  "value": 2
                },
                "bavarian meat loaf": {
                  "value": 3
                },
                "unrelated apple": {
                  "value": 4
                }
              }
            }
            """.trimIndent()
            )

            val report =
                CatalogServerKnowledgeMatchReporter(
                    nearestCandidateLimit =
                        5,
                    queryExpander =
                        NutritionRetrievalQueryExpander()
                )
                    .report(
                        artifactFile =
                            artifactFile,
                        catalogKeys =
                            setOf(
                                "chervil",
                                "leberkaese",
                                "salsify"
                            )
                    )

            val chervil =
                report.unmatched.single {
                    it.catalogKey ==
                            "chervil"
                }

            assertEquals(
                expected =
                    "kerbel raw",
                actual =
                    chervil.nearestCandidates
                        .first()
                        .serverKey
            )

            assertTrue(
                chervil.nearestCandidates
                    .first()
                    .score >
                        0.0
            )

            val leberkaese =
                report.unmatched.single {
                    it.catalogKey ==
                            "leberkaese"
                }

            assertEquals(
                expected =
                    "bavarian meat loaf",
                actual =
                    leberkaese.nearestCandidates
                        .first()
                        .serverKey
            )

            val salsify =
                report.unmatched.single {
                    it.catalogKey ==
                            "salsify"
                }

            assertEquals(
                expected =
                    "black salsify cooked",
                actual =
                    salsify.nearestCandidates
                        .first()
                        .serverKey
            )

        } finally {

            directory.deleteRecursively()
        }
    }

    @Test
    fun rejectsWeakAliasOnlyCandidates() {

        val directory =
            createTempDirectory(
                prefix =
                    "weak-nutrition-alias-report"
            )
                .toFile()

        try {
            val artifactFile =
                File(
                    directory,
                    "nutrition.json"
                )

            artifactFile.writeText(
                """
            {
              "version": 1,
              "entries": {
                "banana spice": {
                  "value": 1
                },
                "barbecue spice": {
                  "value": 2
                },
                "black": {
                  "value": 3
                },
                "black salt": {
                  "value": 4
                },
                "meat loaf": {
                  "value": 5
                },
                "sausage smoked": {
                  "value": 6
                },
                "caramel chocolate candy": {
                  "value": 7
                }
              }
            }
            """.trimIndent()
            )

            val report =
                CatalogServerKnowledgeMatchReporter(
                    nearestCandidateLimit =
                        5,
                    queryExpander =
                        NutritionRetrievalQueryExpander()
                )
                    .report(
                        artifactFile =
                            artifactFile,
                        catalogKeys =
                            setOf(
                                "leberkaese",
                                "mace",
                                "salsify",
                                "teewurst",
                                "toffifee"
                            )
                    )

            val mace =
                report.unmatched.single {
                    it.catalogKey ==
                            "mace"
                }

            assertTrue(
                actual =
                    mace.nearestCandidates.isEmpty(),
                message =
                    "Generic alias token 'spice' must not create " +
                            "nutrition candidates."
            )

            val salsify =
                report.unmatched.single {
                    it.catalogKey ==
                            "salsify"
                }

            assertTrue(
                actual =
                    salsify.nearestCandidates.isEmpty(),
                message =
                    "Generic alias token 'black' must not create " +
                            "nutrition candidates."
            )

            val leberkaese =
                report.unmatched.single {
                    it.catalogKey ==
                            "leberkaese"
                }

            assertEquals(
                expected =
                    "meat loaf",
                actual =
                    leberkaese.nearestCandidates
                        .first()
                        .serverKey
            )

            val teewurst =
                report.unmatched.single {
                    it.catalogKey ==
                            "teewurst"
                }

            assertEquals(
                expected =
                    "sausage smoked",
                actual =
                    teewurst.nearestCandidates
                        .first()
                        .serverKey
            )

            val toffifee =
                report.unmatched.single {
                    it.catalogKey ==
                            "toffifee"
                }

            assertEquals(
                expected =
                    "caramel chocolate candy",
                actual =
                    toffifee.nearestCandidates
                        .first()
                        .serverKey
            )

        } finally {

            directory.deleteRecursively()
        }
    }
}