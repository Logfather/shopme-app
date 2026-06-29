package de.shopme.tools.knowledge.pipeline

interface KnowledgeCandidateBuildPass {

    fun execute(
        result: KnowledgeCandidateBuildResult
    ): KnowledgeCandidateBuildResult

}