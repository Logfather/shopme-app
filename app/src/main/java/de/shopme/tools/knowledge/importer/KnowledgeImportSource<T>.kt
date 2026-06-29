package de.shopme.tools.knowledge.importer

interface KnowledgeImportSource<T> {

    fun read(): Sequence<T>
}