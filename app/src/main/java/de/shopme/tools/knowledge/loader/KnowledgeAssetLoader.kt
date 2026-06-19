package de.shopme.tools.knowledge.loader

interface KnowledgeAssetLoader<T> {

    fun load(): T

}