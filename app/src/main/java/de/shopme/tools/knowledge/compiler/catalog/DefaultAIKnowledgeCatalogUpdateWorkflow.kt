package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchApplier

class DefaultAIKnowledgeCatalogUpdateWorkflow(
    private val patchApplier: FoodsJsonPatchApplier,
    private val catalogWriter: CatalogWriter
) : AIKnowledgeCatalogUpdateWorkflow {

    override fun updateCatalog(
        catalog: List<CatalogItem>,
        patch: FoodsJsonPatch
    ) {
        val updatedCatalog = patchApplier.apply(
            catalog = catalog,
            patch = patch
        )

        catalogWriter.write(updatedCatalog)
    }
}