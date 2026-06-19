package de.shopme.tools.knowledge.compiler.shared.validation.validators.duplicate

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.exception.KnowledgeValidationException
import de.shopme.tools.knowledge.compiler.shared.validation.BuildValidator

class DuplicateFoodReferenceValidator : BuildValidator {

    override fun validate(
        context: KnowledgeBuildContext
    ) {

        val duplicates =

            context.catalog

                .groupBy {
                    it.normalized
                }

                .filterValues {
                    it.size > 1
                }

        if (duplicates.isNotEmpty()) {

            val references =

                duplicates.keys

                    .sorted()

                    .joinToString(", ")

            throw KnowledgeValidationException(

                "Duplicate food references found: $references"

            )

        }

    }

}