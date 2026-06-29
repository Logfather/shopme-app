package de.shopme.tools.knowledge.off

data class CarbonVariantCoverageMergeCandidate(

    val catalogNormalizedName: String,

    val resolvedCarbonReference: String,

    val kilogramsPerKilogram: Double,

    val source: String = "variant_resolver"
)