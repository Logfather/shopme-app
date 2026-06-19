package de.shopme.tools.knowledge.analyzer

data class KnowledgeStatistics(

    val totalCatalogItems: Int,

    val distinctNormalized: Int,

    val distinctNutritionReferences: Int,

    val missingNutritionReferences: Int

)