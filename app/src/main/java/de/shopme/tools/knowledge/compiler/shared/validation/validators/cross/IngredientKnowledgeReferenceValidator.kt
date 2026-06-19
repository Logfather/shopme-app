package de.shopme.tools.knowledge.compiler.shared.validation.validators.cross

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.exception.KnowledgeValidationException
import de.shopme.tools.knowledge.compiler.shared.validation.BuildValidator
import de.shopme.tools.knowledge.ingredients.IngredientsKnowledge
import de.shopme.tools.knowledge.lookup.ArtifactLookup

class IngredientKnowledgeReferenceValidator(

    private val artifactLookup: ArtifactLookup

) : BuildValidator {

    override fun validate(
        context: KnowledgeBuildContext
    ) {

        val knowledge =

            artifactLookup.lookup(
                IngredientsKnowledge::class.java
            ) ?: return

        knowledge.entries.forEach { (food, ingredients) ->

            ingredients.forEach { ingredient ->

                if (!context.containsFood(ingredient)) {

                    throw KnowledgeValidationException(

                        "Unknown ingredient '$ingredient' referenced by '$food'."

                    )

                }

            }

        }

    }
}