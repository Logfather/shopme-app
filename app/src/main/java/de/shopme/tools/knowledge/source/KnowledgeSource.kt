package de.shopme.tools.knowledge.source

interface KnowledgeSource<T : KnowledgeCandidate> {

    fun load(): List<T>
}