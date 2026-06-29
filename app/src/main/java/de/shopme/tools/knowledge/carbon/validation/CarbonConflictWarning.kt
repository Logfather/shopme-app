package de.shopme.tools.knowledge.carbon.validation

data class CarbonConflictWarning(

    val reference: String,

    val minKgCo2ePerKg: Double,

    val maxKgCo2ePerKg: Double,

    val differencePercent: Double

)