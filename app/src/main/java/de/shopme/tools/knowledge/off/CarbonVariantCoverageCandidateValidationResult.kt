package de.shopme.tools.knowledge.off

data class CarbonVariantCoverageCandidateValidationResult(

    val safe: List<CarbonVariantCoverageCandidate>,

    val review: List<InvalidCarbonVariantCoverageCandidate>,

    val invalid: List<InvalidCarbonVariantCoverageCandidate>
)