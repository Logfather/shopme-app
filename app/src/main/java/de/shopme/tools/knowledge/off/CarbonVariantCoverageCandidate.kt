package de.shopme.tools.knowledge.off

data class CarbonVariantCoverageCandidate(

    val catalogNormalizedName: String,

    val resolvedCarbonReference: String,

    val source: String = "variant_resolver"
)