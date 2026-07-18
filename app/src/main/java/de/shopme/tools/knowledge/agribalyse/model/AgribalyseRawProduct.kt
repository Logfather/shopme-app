package de.shopme.tools.knowledge.agribalyse.model

data class AgribalyseRawProduct(
    val agbCode: String,
    val ciqualCode: String,
    val foodGroup: String,
    val foodSubgroup: String,
    val frenchName: String,
    val lciName: String,

    val seasonCode: Int?,
    val airTransportCode: Int?,
    val delivery: String,
    val packagingApproach: String,
    val preparation: String,
    val dataQualityScore: Double?,

    val singleScoreMptPerKg: Double?,
    val carbonKgCo2EqPerKg: Double?,
    val waterM3DeprivationPerKg: Double?,
    val landUsePtPerKg: Double?,
    val energyMjPerKg: Double?,

    val biogenicCarbonKgCo2EqPerKg: Double?,
    val fossilCarbonKgCo2EqPerKg: Double?,
    val landUseChangeCarbonKgCo2EqPerKg: Double?
)