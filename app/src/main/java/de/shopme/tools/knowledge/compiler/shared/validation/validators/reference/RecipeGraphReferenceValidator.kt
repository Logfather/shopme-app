package de.shopme.tools.knowledge.compiler.shared.validation.validators.reference

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.exception.KnowledgeValidationException
import de.shopme.tools.knowledge.compiler.shared.validation.BuildValidator
import de.shopme.tools.knowledge.recipe.RecipeKnowledge

class RecipeGraphReferenceValidator : BuildValidator {

    override fun validate(
        context: KnowledgeBuildContext
    ) {

        context.artifacts

            .filterIsInstance<RecipeKnowledge>()

            .forEach { knowledge ->

                knowledge.entries.forEach { (recipe, ingredients) ->

                    ingredients.forEach { ingredient ->

                        if (!context.containsFood(ingredient)) {

                            throw KnowledgeValidationException(

                                "Unknown recipe reference '$ingredient' in '$recipe'."

                            )

                        }

                    }

                }

            }

    }

}