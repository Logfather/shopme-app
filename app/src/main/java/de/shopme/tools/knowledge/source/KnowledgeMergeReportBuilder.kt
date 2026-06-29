package de.shopme.tools.knowledge.source

interface KnowledgeMergeReportBuilder<
        TCandidate : KnowledgeCandidate
        > {

    fun build(

        candidates: List<TCandidate>,

        merged: Map<String, TCandidate>

    ): KnowledgeMergeReport<TCandidate>
}