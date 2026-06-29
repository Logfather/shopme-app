package de.shopme.tools.knowledge.source

interface KnowledgeMerger<
        TCandidate : KnowledgeCandidate
        > {

    fun merge(
        candidates: List<TCandidate>
    ): Map<String, TCandidate>
}