package de.shopme.testing.system.tools.knowledge.fixture

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.CompilerContext

object CompilerContextFixture {

    fun create(
        nutritionReference: String? = null,
        normalizedName: String = nutritionReference ?: "test"
    ): CompilerContext {

        return CompilerContext(

            catalogItem =

                CatalogItem(

                    itemname = "Test",

                    category = "",

                    production = "",

                    normalized = normalizedName,

                    plural = "",

                    colloquial = emptyList(),

                    phonetic_tokens = emptyList(),

                    autocomplete_tokens = emptyList(),

                    nutritionReference = nutritionReference

                )

        )

    }

    fun milk() =
        create(
            nutritionReference = "milk"
        )

    fun unknown() =
        create(
            nutritionReference = "unknown"
        )

}