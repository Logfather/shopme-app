package de.shopme.tools.knowledge.off

class CarbonVariantCoverageCandidateValidator {

    fun validate(
        candidates: List<CarbonVariantCoverageCandidate>
    ): CarbonVariantCoverageCandidateValidationResult {

        val safe =
            mutableListOf<CarbonVariantCoverageCandidate>()

        val review =
            mutableListOf<InvalidCarbonVariantCoverageCandidate>()

        val invalid =
            mutableListOf<InvalidCarbonVariantCoverageCandidate>()

        candidates.forEach { candidate ->

            val hardReasons =
                validateHardRules(candidate)

            if (hardReasons.isNotEmpty()) {

                invalid += InvalidCarbonVariantCoverageCandidate(
                    candidate = candidate,
                    reasons = hardReasons
                )

                return@forEach
            }

            val reviewReasons =
                validateReviewRules(candidate)

            if (reviewReasons.isNotEmpty()) {

                review += InvalidCarbonVariantCoverageCandidate(
                    candidate = candidate,
                    reasons = reviewReasons
                )

            } else {

                safe += candidate
            }
        }

        return CarbonVariantCoverageCandidateValidationResult(
            safe = safe,
            review = review,
            invalid = invalid
        )
    }

    private fun validateHardRules(
        candidate: CarbonVariantCoverageCandidate
    ): List<String> {

        val reasons =
            mutableListOf<String>()

        if (candidate.catalogNormalizedName.isBlank()) {
            reasons += "catalogNormalizedName is blank"
        }

        if (candidate.resolvedCarbonReference.isBlank()) {
            reasons += "resolvedCarbonReference is blank"
        }

        if (candidate.source != "variant_resolver") {
            reasons += "source must be variant_resolver"
        }

        if (candidate.catalogNormalizedName == candidate.resolvedCarbonReference) {
            reasons += "candidate maps to itself"
        }

        return reasons
    }

    private fun validateReviewRules(
        candidate: CarbonVariantCoverageCandidate
    ): List<String> {

        val reasons =
            mutableListOf<String>()

        val productTokens =
            candidate.catalogNormalizedName
                .split(" ")
                .filter {
                    it.isNotBlank()
                }

        if (
            productTokens.any {
                it in compoundMealTokens
            }
        ) {
            reasons += "compound meal token"
        }

        if (
            productTokens.any {
                it in riskyContextTokens
            }
        ) {
            reasons += "risky context token"
        }

        if (
            candidate.resolvedCarbonReference in riskyBaseReferences
        ) {
            reasons += "risky base reference"
        }

        return reasons
    }

    private companion object {

        val compoundMealTokens =
            setOf(
                "fertiggericht",
                "gericht",
                "pfanne",
                "auflauf",
                "eintopf",
                "salat",
                "burger",
                "wrap",
                "sandwich",
                "bolognese",
                "curry",
                "suppe"
            )

        val riskyContextTokens =
            setOf(
                "alternative",
                "ersatz",
                "vegan",
                "vegane",
                "vegetarisch",
                "vegetarische",
                "gemuese",
                "gemuesemix",
                "tiefkuehlgemuese",
                "mischung",
                "mix",
                "mit",
                "fuellung",
                "gefuellt"
            )

        val riskyBaseReferences =
            setOf(
                "brot",
                "butter",
                "chili",
                "reis",
                "pasta",
                "nudeln",
                "sauce",
                "sosse",
                "gewuerz"
            )
    }
}