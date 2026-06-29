package de.shopme.tools.knowledge.off

data class OFFCarbonKnowledgeArtifactCandidate(

    val catalogNormalizedName: String,

    val kilogramsCo2PerKilogram: Double,

    val source: String,

    val reference: String
)