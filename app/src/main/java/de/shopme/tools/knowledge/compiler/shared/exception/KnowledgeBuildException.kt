package de.shopme.tools.knowledge.compiler.shared.exception

open class KnowledgeBuildException(

    message: String,
    cause: Throwable? = null

) : RuntimeException(message, cause)