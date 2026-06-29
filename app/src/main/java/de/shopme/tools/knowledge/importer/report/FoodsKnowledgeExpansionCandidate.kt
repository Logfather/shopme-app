package de.shopme.tools.knowledge.foods.importer.report

data class FoodsKnowledgeExpansionCandidate(

    val name: String,

    val mappedReference: String,

    val canonicalExists: Boolean,

    val occurrences: Int
)