package de.shopme.tools.knowledge.compiler.shared.validation.validators.reference

import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareKnowledge
import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.exception.KnowledgeValidationException
import de.shopme.tools.knowledge.compiler.shared.validation.BuildValidator
import de.shopme.tools.knowledge.lookup.ArtifactLookup

class AnimalWelfareKnowledgeReferenceValidator(

    private val artifactLookup: ArtifactLookup

) : BuildValidator {

    override fun validate(
        context: KnowledgeBuildContext
    ) {

        val knowledge =
            artifactLookup.lookup(
                AnimalWelfareKnowledge::class.java
            ) ?: return

        knowledge.entries.forEach { (food, _) ->

            if (!context.containsFood(food)) {

                throw KnowledgeValidationException(
                    "Unknown animal welfare reference '$food'."
                )

            }

        }

    }
}