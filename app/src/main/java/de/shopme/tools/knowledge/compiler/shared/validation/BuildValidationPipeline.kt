package de.shopme.tools.knowledge.compiler.shared.validation

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext

class BuildValidationPipeline(

    private val validators: List<BuildValidator>

) {

    fun validate(
        context: KnowledgeBuildContext
    ) {

        validators.forEach { validator ->

            validator.validate(
                context
            )
        }
    }
}