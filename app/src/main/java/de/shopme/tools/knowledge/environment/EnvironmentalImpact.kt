package de.shopme.tools.knowledge.environment

data class EnvironmentalImpact(
    val environmentScoreMptPerKg: Double,
    val climateKgCo2EqPerKg: Double,
    val landUsePtPerKg: Double,
    val waterDeprivationM3PerKg: Double
)