package de.shopme.testing.system.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch
import de.shopme.tools.knowledge.compiler.candidate.DefaultFoodsJsonPatchApplier
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperation
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperationType
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFoodsJsonPatchApplierTest {

    @Test
    fun givenEmptyPatch_returnsOriginalCatalog() {

        val catalog = listOf(
            CatalogItem(
                itemname = "Apple",
                category = "fruit",
                production = "plant_based",
                normalized = "apple",
                plural = "apples",
                colloquial = listOf("Apfel"),
                phonetic_tokens = listOf("apple"),
                autocomplete_tokens = listOf("apple")
            )
        )

        val patch = FoodsJsonPatch(
            operations = emptyList()
        )

        val result = DefaultFoodsJsonPatchApplier()
            .apply(
                catalog = catalog,
                patch = patch
            )

        assertEquals(catalog, result)
    }

    @Test
    fun addPatchOperation_addsCatalogItem() {

        val catalog = emptyList<CatalogItem>()

        val patch = FoodsJsonPatch(
            operations = listOf(
                FoodsJsonPatchOperation(
                    canonicalId = "apple",
                    type = FoodsJsonPatchOperationType.ADD,
                    candidate = CandidateFoodKnowledgePatch(
                        canonicalId = "apple",
                        aliases = setOf("Apple", "Apfel"),
                        dimensions = emptyList(),
                        metadata = CandidateMetadata(
                            source = "open_food_facts",
                            sourceId = "123",
                            confidence = 1.0,
                            version = "manual-v1"
                        )
                    )
                )
            )
        )

        val result = DefaultFoodsJsonPatchApplier()
            .apply(
                catalog = catalog,
                patch = patch
            )

        assertEquals(1, result.size)
        assertEquals("apple", result.first().normalized)
        assertEquals("Apple", result.first().itemname)
    }

    @Test
    fun addPatchOperationForExistingCatalogItem_doesNotAddDuplicate() {

        val catalog = listOf(
            CatalogItem(
                itemname = "Apple",
                category = "fruit",
                production = "plant_based",
                normalized = "apple",
                plural = "apples",
                colloquial = listOf("Apfel"),
                phonetic_tokens = listOf("apple"),
                autocomplete_tokens = listOf("apple")
            )
        )

        val patch = FoodsJsonPatch(
            operations = listOf(
                FoodsJsonPatchOperation(
                    canonicalId = "apple",
                    type = FoodsJsonPatchOperationType.ADD,
                    candidate = CandidateFoodKnowledgePatch(
                        canonicalId = "apple",
                        aliases = setOf("Apple", "Apfel"),
                        dimensions = emptyList(),
                        metadata = CandidateMetadata(
                            source = "open_food_facts",
                            sourceId = "123",
                            confidence = 1.0,
                            version = "manual-v1"
                        )
                    )
                )
            )
        )

        val result = DefaultFoodsJsonPatchApplier()
            .apply(
                catalog = catalog,
                patch = patch
            )

        assertEquals(1, result.size)
        assertEquals(catalog.first(), result.first())
    }

    @Test
    fun updatePatchOperation_updatesExistingCatalogItem() {

        val catalog = listOf(
            CatalogItem(
                itemname = "Apple",
                category = "fruit",
                production = "plant_based",
                normalized = "apple",
                plural = "apples",
                colloquial = listOf("Old Alias"),
                phonetic_tokens = listOf("apple"),
                autocomplete_tokens = listOf("apple")
            )
        )

        val patch = FoodsJsonPatch(
            operations = listOf(
                FoodsJsonPatchOperation(
                    canonicalId = "apple",
                    type = FoodsJsonPatchOperationType.UPDATE,
                    candidate = CandidateFoodKnowledgePatch(
                        canonicalId = "apple",
                        aliases = setOf("Apple", "Apfel"),
                        dimensions = emptyList(),
                        metadata = CandidateMetadata(
                            source = "open_food_facts",
                            sourceId = "123",
                            confidence = 1.0,
                            version = "manual-v1"
                        )
                    )
                )
            )
        )

        val result = DefaultFoodsJsonPatchApplier()
            .apply(
                catalog = catalog,
                patch = patch
            )

        assertEquals(1, result.size)
        assertEquals(listOf("Apple", "Apfel"), result.first().colloquial)
        assertEquals("apple", result.first().normalized)
    }

    @Test
    fun updatePatchOperationForMissingCatalogItem_isIgnored() {

        val catalog = emptyList<CatalogItem>()

        val patch = FoodsJsonPatch(
            operations = listOf(
                FoodsJsonPatchOperation(
                    canonicalId = "apple",
                    type = FoodsJsonPatchOperationType.UPDATE,
                    candidate = CandidateFoodKnowledgePatch(
                        canonicalId = "apple",
                        aliases = setOf("Apple", "Apfel"),
                        dimensions = emptyList(),
                        metadata = CandidateMetadata(
                            source = "open_food_facts",
                            sourceId = "123",
                            confidence = 1.0,
                            version = "manual-v1"
                        )
                    )
                )
            )
        )

        val result = DefaultFoodsJsonPatchApplier()
            .apply(
                catalog = catalog,
                patch = patch
            )

        assertTrue(result.isEmpty())
    }

    @Test
    fun applyReturnsCatalogSortedByNormalized() {

        val catalog = listOf(
            catalogItemWithNormalized("tomato"),
            catalogItemWithNormalized("apple")
        )

        val patch = FoodsJsonPatch(
            operations = listOf(
                FoodsJsonPatchOperation(
                    type = FoodsJsonPatchOperationType.ADD,
                    canonicalId = "banana",
                    candidate = CandidateFoodKnowledgePatch(
                        canonicalId = "banana",
                        aliases = emptySet(),
                        dimensions = emptyList(),
                        metadata = CandidateMetadata(
                            source = "test",
                            sourceId = null,
                            confidence = 1.0,
                            version = null
                        )
                    )
                )
            )
        )

        val result = DefaultFoodsJsonPatchApplier().apply(
            catalog = catalog,
            patch = patch
        )

        assertEquals(
            listOf(
                "apple",
                "banana",
                "tomato"
            ),
            result.map {
                it.normalized
            }
        )
    }

    @Test
    fun applyDoesNotReturnDuplicateCatalogItemsAfterAdd() {

        val catalog = listOf(
            catalogItemWithNormalized("apple")
        )

        val patch = FoodsJsonPatch(
            operations = listOf(
                FoodsJsonPatchOperation(
                    type = FoodsJsonPatchOperationType.ADD,
                    canonicalId = "apple",
                    candidate = CandidateFoodKnowledgePatch(
                        canonicalId = "apple",
                        aliases = emptySet(),
                        dimensions = emptyList(),
                        metadata = CandidateMetadata(
                            source = "test",
                            sourceId = null,
                            confidence = 1.0,
                            version = null
                        )
                    )
                )
            )
        )

        val result = DefaultFoodsJsonPatchApplier().apply(
            catalog = catalog,
            patch = patch
        )

        assertEquals(
            listOf("apple"),
            result.map { it.normalized }
        )
    }

    @Test
    fun addCreatesCatalogItemWithSortedAliasesAndTokens() {
        // arrange: aliases unsortiert
        // act: ADD anwenden
        // assert:
        // colloquial alphabetisch
        // phonetic_tokens alphabetisch
        // autocomplete_tokens alphabetisch
    }

    private fun catalogItemWithNormalized(
        normalized: String
    ): CatalogItem =
        CatalogItem(
            itemname = normalized,
            category = "",
            production = "",
            normalized = normalized,
            plural = "",
            colloquial = emptyList(),
            phonetic_tokens = emptyList(),
            autocomplete_tokens = emptyList()
        )
}