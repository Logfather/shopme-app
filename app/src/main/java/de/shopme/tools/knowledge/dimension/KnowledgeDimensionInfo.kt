package de.shopme.tools.knowledge.dimension

data class KnowledgeDimensionInfo(

    val title: String,

    val description: String,

    val storedFacts: List<String>,

    val evaluation: String,

    val interpretations: List<KnowledgeDimensionInterpretation>

)