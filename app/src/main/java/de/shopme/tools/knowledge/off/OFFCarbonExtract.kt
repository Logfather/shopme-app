package de.shopme.tools.knowledge.off

data class OFFCarbonExtract(

    val co2Total: Double? = null,

    val co2Agriculture: Double? = null,

    val co2Processing: Double? = null,

    val co2Packaging: Double? = null,

    val co2Transportation: Double? = null
)