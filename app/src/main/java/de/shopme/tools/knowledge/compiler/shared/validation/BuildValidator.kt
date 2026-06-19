package de.shopme.tools.knowledge.compiler.shared.validation

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext

interface BuildValidator {

    fun validate(
        context: KnowledgeBuildContext
    )

}