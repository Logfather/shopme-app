package de.shopme.tools.knowledge.compiler.shared.writer

import de.shopme.tools.knowledge.compiler.CompilerContext

abstract class KnowledgeWriter {

    abstract fun begin()

    abstract fun write(
        context: CompilerContext
    )

    abstract fun finish()

}