package de.shopme.testing.system.tools.knowledge.artifacts

data class FoodsKnowledgeArtifactComparison(

    val existingCount: Int,
    val generatedCount: Int,

    val missingInGenerated: List<String>,
    val missingInExisting: List<String>

)