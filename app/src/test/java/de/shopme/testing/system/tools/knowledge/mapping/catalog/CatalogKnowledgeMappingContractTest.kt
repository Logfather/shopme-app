package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMapping
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMappingContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMappingStrategy
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMappings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CatalogKnowledgeMappingContractTest {

    @Test
    fun createsVersionedCatalogKnowledgeMappings() {

        val mapping =
            CatalogKnowledgeMapping(
                catalogKey = "semi skimmed milk",
                serverKey = "milk semi skimmed uht",
                serverArtifact =
                    "environmental_impact.json",
                strategy =
                    CatalogKnowledgeMappingStrategy.AI,
                confidence = 0.97,
                reason =
                    "Catalog product describes semi-skimmed UHT milk"
            )

        val artifact =
            CatalogKnowledgeMappings(
                version =
                    CatalogKnowledgeMappingContract.CURRENT_VERSION,
                mappings = listOf(mapping)
            )

        assertEquals(
            1,
            artifact.version
        )

        assertEquals(
            mapping,
            artifact.mappings.single()
        )
    }


    @Test
    fun acceptsSameCatalogKeyForDifferentArtifacts() {

        val artifact =
            CatalogKnowledgeMappings(
                version =
                    CatalogKnowledgeMappingContract.CURRENT_VERSION,
                mappings = listOf(
                    CatalogKnowledgeMapping(
                        catalogKey = "semi skimmed milk",
                        serverKey = "milk semi skimmed uht",
                        serverArtifact =
                            "environmental_impact.json",
                        strategy =
                            CatalogKnowledgeMappingStrategy.AI,
                        confidence = 0.97
                    ),
                    CatalogKnowledgeMapping(
                        catalogKey = "semi skimmed milk",
                        serverKey = "milk semi skimmed uht",
                        serverArtifact =
                            "water_footprint.json",
                        strategy =
                            CatalogKnowledgeMappingStrategy.AI,
                        confidence = 0.97
                    )
                )
            )

        assertEquals(
            2,
            artifact.mappings.size
        )
    }


    @Test
    fun rejectsMultipleMappingsForSameCatalogKeyAndArtifact() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMappings(
                version =
                    CatalogKnowledgeMappingContract.CURRENT_VERSION,
                mappings = listOf(
                    CatalogKnowledgeMapping(
                        catalogKey = "semi skimmed milk",
                        serverKey = "milk semi skimmed uht",
                        serverArtifact =
                            "environmental_impact.json",
                        strategy =
                            CatalogKnowledgeMappingStrategy.AI,
                        confidence = 0.97
                    ),
                    CatalogKnowledgeMapping(
                        catalogKey = "semi skimmed milk",
                        serverKey =
                            "milk semi skimmed pasteurized",
                        serverArtifact =
                            "environmental_impact.json",
                        strategy =
                            CatalogKnowledgeMappingStrategy.AI,
                        confidence = 0.82
                    )
                )
            )
        }
    }


    @Test
    fun rejectsInvalidConfidence() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMapping(
                catalogKey = "semi skimmed milk",
                serverKey = "milk semi skimmed uht",
                serverArtifact =
                    "environmental_impact.json",
                strategy =
                    CatalogKnowledgeMappingStrategy.AI,
                confidence = 1.01
            )
        }
    }


    @Test
    fun rejectsBlankKeys() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMapping(
                catalogKey = " ",
                serverKey = "milk semi skimmed uht",
                serverArtifact =
                    "environmental_impact.json",
                strategy =
                    CatalogKnowledgeMappingStrategy.EXACT,
                confidence = 1.0
            )
        }
    }
}