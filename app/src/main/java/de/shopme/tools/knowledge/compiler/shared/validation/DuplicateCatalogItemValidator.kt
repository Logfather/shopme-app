package de.shopme.tools.knowledge.compiler.shared.validation

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.exception.KnowledgeBuildException

class DuplicateCatalogItemValidator : BuildValidator {

    override fun validate(
        context: KnowledgeBuildContext
    ) {

        validateUnique(
            label = "itemname",
            values =
                context.catalog.map {
                    it.itemname.trim().lowercase()
                }
        )

        validateUnique(
            label = "normalized",
            values =
                context.catalog.map {
                    it.normalized.trim().lowercase()
                }
        )

        validateUnique(
            label = "nutritionReference",
            values =
                context.catalog
                    .mapNotNull {
                        it.nutritionReference
                            ?.trim()
                            ?.lowercase()
                            ?.takeIf { value -> value.isNotBlank() }
                    }
        )
    }

    private fun validateUnique(
        label: String,
        values: List<String>
    ) {

        val duplicates =
            values
                .groupingBy { it }
                .eachCount()
                .filterValues { count ->
                    count > 1
                }
                .keys

        if (duplicates.isNotEmpty()) {
            throw KnowledgeBuildException(
                "Duplicate catalog $label values found: ${
                    duplicates.sorted().joinToString()
                }"
            )
        }
    }
}