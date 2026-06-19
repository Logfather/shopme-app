package de.shopme.tools.knowledge.source

interface FoodKnowledgeSource<T> {

    fun load(): T

}