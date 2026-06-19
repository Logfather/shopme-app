package de.shopme.tools.knowledge.compiler.shared.validation.validators.reference

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.exception.KnowledgeValidationException
import de.shopme.tools.knowledge.compiler.shared.validation.BuildValidator
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphKnowledge

class IngredientGraphReferenceValidator : BuildValidator {

    override fun validate(
        context: KnowledgeBuildContext
    ) {

        context.artifacts

            .filterIsInstance<IngredientGraphKnowledge>()

            .forEach { knowledge ->

                knowledge.entries.forEach { (food, graphEntry) ->

                    graphEntry.ingredients.forEach { ingredient ->

                        if (!context.containsFood(ingredient)) {

                            throw KnowledgeValidationException(

                                "Unknown ingredient reference '$ingredient' in '$food'."

                            )

                        }

                    }

                }

            }

    }

}