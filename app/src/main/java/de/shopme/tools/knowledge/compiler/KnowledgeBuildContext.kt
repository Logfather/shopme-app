package de.shopme.tools.knowledge.compiler

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.KnowledgeArtifact

data class KnowledgeBuildContext(

    /**
     * Complete catalog used for this build.
     */
    val catalog: List<CatalogItem>,

    /**
     * Already generated knowledge artifacts.
     * Can be used by cross-artifact validators.
     */
    val artifacts: List<KnowledgeArtifact> = emptyList()

) {

    /**
     * All canonical food references known to the build.
     */
    val knownFoodReferences: Set<String> =
        catalog
            .map { it.normalized }
            .toSet()

    /**
     * Fast lookup by canonical name.
     */
    val catalogByName: Map<String, CatalogItem> =
        catalog.associateBy { it.normalized }

    /**
     * Returns true if a food reference exists.
     */
    fun containsFood(
        reference: String
    ): Boolean =
        reference in knownFoodReferences

    /**
     * Returns the catalog item for a canonical reference.
     */
    fun findFood(
        reference: String
    ): CatalogItem? =
        catalogByName[reference]

}