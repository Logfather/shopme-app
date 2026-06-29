package de.shopme.tools.knowledge.off

class OFFCarbonKnowledgeArtifactCandidateValidator {

    fun isValid(
        candidate: OFFCarbonKnowledgeArtifactCandidate
    ): Boolean {

        if (
            candidate.catalogNormalizedName.isBlank()
        ) {
            return false
        }

        if (
            candidate.source != "off"
        ) {
            return false
        }

        if (
            candidate.reference.isBlank()
        ) {
            return false
        }

        if (
            !candidate.reference.startsWith("off:")
        ) {
            return false
        }

        if (
            candidate.kilogramsCo2PerKilogram <= 0.0
        ) {
            return false
        }

        if (
            candidate.kilogramsCo2PerKilogram >= 100.0
        ) {
            return false
        }

        return true
    }
}