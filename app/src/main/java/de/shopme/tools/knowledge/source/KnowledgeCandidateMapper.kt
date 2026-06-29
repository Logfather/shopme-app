package de.shopme.tools.knowledge.source

interface KnowledgeCandidateMapper<
        TCandidate : KnowledgeCandidate,
        TResult
        > {

    fun map(
        candidate: TCandidate
    ): TResult
}