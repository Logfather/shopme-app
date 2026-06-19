package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext

interface FoodKnowledgeWriter {

    fun begin() {}

    fun write(

        context: CompilerContext

    )

    fun finish() {}

}