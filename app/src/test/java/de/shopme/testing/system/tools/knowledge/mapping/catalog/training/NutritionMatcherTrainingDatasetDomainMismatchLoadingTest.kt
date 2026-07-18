package de.shopme.testing.system.tools.knowledge.mapping.catalog.training

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingDataset
import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherCandidate
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NutritionMatcherTrainingDatasetDomainMismatchLoadingTest {

    private val datasetFile =
        File(
            "../data/generated/knowledge/training/" +
                    "nutrition.matcher-training-dataset.json",
        )

    @Test
    fun loadDomainMismatchFeaturesForEveryTrainingExample() {

        val dataset =
            loadDataset()

        assertEquals(
            expected = 4256,
            actual = dataset.examples.size,
        )

        assertTrue(
            dataset.examples.all { example ->
                example.domainMismatchFeatures != null
            },
            "Every persisted nutrition training example must expose " +
                    "its Domain-Mismatch features after Gson deserialization.",
        )

        assertTrue(
            dataset.examples.all { example ->
                example.domainMismatchFeatures?.version == 1
            },
            "Every Domain-Mismatch feature record must use version 1.",
        )
    }

    @Test
    fun preserveReportedAndDefaultRelationshipsAfterLoading() {

        val dataset =
            loadDataset()

        val withReportRelationship =
            dataset.examples.count { example ->
                example.domainMismatchFeatures
                    ?.reportRelationshipPresent == true
            }

        val withoutReportRelationship =
            dataset.examples.count { example ->
                example.domainMismatchFeatures
                    ?.reportRelationshipPresent == false
            }

        assertEquals(
            expected = 395,
            actual = withReportRelationship,
        )

        assertEquals(
            expected = 3861,
            actual = withoutReportRelationship,
        )

        assertEquals(
            expected = dataset.examples.size,
            actual =
                withReportRelationship +
                        withoutReportRelationship,
        )
    }

    @Test
    fun defaultFeaturesRemainDeterministicAfterLoading() {

        val dataset =
            loadDataset()

        val example =
            dataset.examples.first { trainingExample ->
                trainingExample.domainMismatchFeatures
                    ?.reportRelationshipPresent == false
            }

        val features =
            assertNotNull(
                example.domainMismatchFeatures,
            )

        assertEquals(
            expected = 1,
            actual = features.version,
        )

        assertFalse(
            features.reportRelationshipPresent,
        )

        assertEquals(
            expected = 0,
            actual = features.observationCount,
        )

        assertEquals(
            expected = 0,
            actual =
                features.knownSemanticObservationCount,
        )

        assertEquals(
            expected = 0,
            actual =
                features.unknownSemanticObservationCount,
        )
    }

    @Test
    fun transportDomainMismatchFeaturesIntoLocalCandidate() {

        val dataset =
            loadDataset()

        val example =
            dataset.examples.first { trainingExample ->
                trainingExample.domainMismatchFeatures
                    ?.reportRelationshipPresent == true
            }

        val candidate =
            LocalNutritionMatcherCandidate(
                catalogKey =
                    example.catalogKey,
                serverKey =
                    example.serverKey,
                candidateRank =
                    example.candidateRank,
                candidateCount =
                    example.candidateCount,
                diagnosticScore =
                    example.diagnosticScore,
                diagnosticScoreAvailable =
                    example.diagnosticScoreAvailable,
                sharedTokens =
                    example.sharedTokens,
                domainMismatchFeatures =
                    example.domainMismatchFeatures,
            )

        val candidateFeatures =
            assertNotNull(
                candidate.domainMismatchFeatures,
            )

        assertTrue(
            candidateFeatures.reportRelationshipPresent,
        )

        assertEquals(
            expected =
                example.domainMismatchFeatures,
            actual =
                candidate.domainMismatchFeatures,
        )
    }

    private fun loadDataset():
            NutritionMatcherTrainingDataset {

        require(datasetFile.isFile) {
            "Nutrition matcher training dataset does not exist: " +
                    datasetFile.absolutePath
        }

        return GsonBuilder()
            .create()
            .fromJson(
                datasetFile.readText(),
                NutritionMatcherTrainingDataset::class.java,
            )
    }
}