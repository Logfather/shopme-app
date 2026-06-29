package de.shopme.tools.knowledge.off

data class InvalidCarbonVariantCoverageCandidate(

    val candidate: CarbonVariantCoverageCandidate,

    val reasons: List<String>
)