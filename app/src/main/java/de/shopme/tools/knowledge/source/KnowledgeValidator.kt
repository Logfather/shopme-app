package de.shopme.tools.knowledge.source

interface KnowledgeValidator<
        TReport,
        TResult
        > {

    fun validate(
        report: TReport
    ): TResult
}