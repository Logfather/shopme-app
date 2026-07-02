package de.shopme.testing.system.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.candidate.DefaultFoodsJsonPatchApplier
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperation
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperationType
import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNull

class DefaultFoodsJsonPatchApplierKnowledgeTest {

    private val applier =
        DefaultFoodsJsonPatchApplier()

    @Test
    fun writesKnowledgeReferencesForAddedCatalogItems() {

        val patch =
            FoodsJsonPatch(
                operations = listOf(
                    FoodsJsonPatchOperation(
                        canonicalId = "00000758",
                        type = FoodsJsonPatchOperationType.ADD,
                        candidate = CandidateFoodKnowledgePatch(
                            canonicalId = "00000758",
                            aliases = setOf("Test Food"),
                            dimensions = listOf(
                                testDimension(KnowledgeDimensionCandidateType.NUTRITION),
                                testDimension(KnowledgeDimensionCandidateType.INGREDIENTS),
                                testDimension(KnowledgeDimensionCandidateType.TAXONOMY)
                            ),
                            metadata = CandidateMetadata(
                                "Open Food Facts",
                                "00000758",
                                1.0,
                                "test"
                            )
                        )
                    )
                )
            )

        val result =
            applier.apply(
                catalog = emptyList(),
                patch = patch
            )

        val item =
            result.single()

        assertEquals("00000758", item.knowledge?.nutrition?.reference)
        assertEquals("Open Food Facts", item.knowledge?.nutrition?.source)

        assertEquals("00000758", item.knowledge?.ingredients?.reference)
        assertEquals("Open Food Facts", item.knowledge?.ingredients?.source)

        assertEquals("00000758", item.knowledge?.taxonomy?.reference)
        assertEquals("Open Food Facts", item.knowledge?.taxonomy?.source)
    }

    @Test
    fun updatesKnowledgeReferencesForExistingCatalogItems() {

        val existingItem =
            CatalogItem(
                itemname = "Old Food",
                category = "unknown",
                production = "unknown",
                normalized = "00000758",
                plural = "00000758",
                colloquial = emptyList(),
                phonetic_tokens = emptyList(),
                autocomplete_tokens = emptyList()
            )

        val patch =
            FoodsJsonPatch(
                operations = listOf(
                    FoodsJsonPatchOperation(
                        canonicalId = "00000758",
                        type = FoodsJsonPatchOperationType.UPDATE,
                        candidate = CandidateFoodKnowledgePatch(
                            canonicalId = "00000758",
                            aliases = setOf("Updated Food"),
                            dimensions = listOf(
                                testDimension(KnowledgeDimensionCandidateType.NUTRITION),
                                testDimension(KnowledgeDimensionCandidateType.INGREDIENTS)
                            ),
                            metadata = CandidateMetadata(
                                "Open Food Facts",
                                "00000758",
                                1.0,
                                "test"
                            )
                        )
                    )
                )
            )

        val result =
            applier.apply(
                catalog = listOf(existingItem),
                patch = patch
            )

        val item =
            result.single()

        assertEquals("00000758", item.knowledge?.nutrition?.reference)
        assertEquals("Open Food Facts", item.knowledge?.nutrition?.source)

        assertEquals("00000758", item.knowledge?.ingredients?.reference)
        assertEquals("Open Food Facts", item.knowledge?.ingredients?.source)

        assertNull(item.knowledge?.taxonomy)
    }

    private fun testDimension(
        type: KnowledgeDimensionCandidateType
    ): KnowledgeDimensionCandidate =
        KnowledgeDimensionCandidate(
            dimension = type,
            payload = Unit
        )
}