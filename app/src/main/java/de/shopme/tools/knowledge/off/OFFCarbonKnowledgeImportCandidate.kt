package de.shopme.tools.knowledge.off

data class OFFCarbonKnowledgeImportCandidate(

    val catalogNormalizedName: String,

    val source: String,

    val reference: String,

    val offCode: String?,

    val offProductName: String?
)