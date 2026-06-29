package de.shopme.tools.knowledge.foods.importer.report

data class FoodsKnowledgeMatchReport(

    val incomingFoods: Int,

    val matchedFoods: Int,

    val unmatchedFoods: Int,

    val unmatchedNames: Map<String, Int>,

    val unmatchedOccurrences: Map<String, Int>

)