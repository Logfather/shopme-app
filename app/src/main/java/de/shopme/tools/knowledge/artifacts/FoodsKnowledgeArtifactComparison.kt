package de.shopme.tools.knowledge.artifacts

data class FoodsKnowledgeArtifactComparison(

    val existingCount: Int,
    val generatedCount: Int,

    val missingInGenerated: List<String>,
    val missingInExisting: List<String>

)