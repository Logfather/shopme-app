package de.shopme.tools.knowledge.compiler.shared.validation.validators.reference

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.exception.KnowledgeValidationException
import de.shopme.tools.knowledge.compiler.shared.validation.BuildValidator
import de.shopme.tools.knowledge.foodmiles.FoodMilesKnowledge
import de.shopme.tools.knowledge.lookup.ArtifactLookup

class FoodMilesKnowledgeReferenceValidator(

    private val artifactLookup: ArtifactLookup

) : BuildValidator {

    override fun validate(
        context: KnowledgeBuildContext
    ) {

        val knowledge =
            artifactLookup.lookup(
                FoodMilesKnowledge::class.java
            ) ?: return

        knowledge.entries.forEach { (food, _) ->

            if (!context.containsFood(food)) {

                throw KnowledgeValidationException(
                    "Unknown food miles reference '$food'."
                )

            }

        }
    }
}