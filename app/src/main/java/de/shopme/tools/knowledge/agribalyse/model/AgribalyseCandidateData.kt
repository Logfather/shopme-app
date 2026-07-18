package de.shopme.tools.knowledge.agribalyse.model

data class AgribalyseCandidateData(
    val sourceId: String,
    val name: String?,

    val taxonomy: List<String>,
    val production: List<String>,

    val carbon: Double?,
    val water: Double?,

    val dataQualityScore: Double?,
    val singleScoreMptPerKg: Double?,
    val landUsePtPerKg: Double?,
    val energyMjPerKg: Double?,

    val biogenicCarbonKgCo2EqPerKg: Double?,
    val fossilCarbonKgCo2EqPerKg: Double?,
    val landUseChangeCarbonKgCo2EqPerKg: Double?
)