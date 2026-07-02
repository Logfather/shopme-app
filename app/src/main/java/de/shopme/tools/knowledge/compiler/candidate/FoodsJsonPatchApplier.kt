package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem

interface FoodsJsonPatchApplier {

    fun apply(
        catalog: List<CatalogItem>,
        patch: FoodsJsonPatch
    ): List<CatalogItem>
}