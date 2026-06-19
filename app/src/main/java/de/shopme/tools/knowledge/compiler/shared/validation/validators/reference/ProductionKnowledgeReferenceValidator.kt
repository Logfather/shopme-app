package de.shopme.tools.knowledge.compiler.shared.validation.validators.reference

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.exception.KnowledgeValidationException
import de.shopme.tools.knowledge.compiler.shared.validation.BuildValidator
import de.shopme.tools.knowledge.lookup.ArtifactLookup
import de.shopme.tools.knowledge.production.ProductionKnowledge

class ProductionKnowledgeReferenceValidator(

    private val artifactLookup: ArtifactLookup

) : BuildValidator {

    override fun validate(
        context: KnowledgeBuildContext
    ) {

        val knowledge =
            artifactLookup.lookup(
                ProductionKnowledge::class.java
            ) ?: return

        knowledge.entries.forEach { (food, _) ->

            if (!context.containsFood(food)) {

                throw KnowledgeValidationException(
                    "Unknown production reference '$food'."
                )

            }

        }

    }

}