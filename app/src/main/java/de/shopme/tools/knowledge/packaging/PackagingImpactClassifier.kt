package de.shopme.tools.knowledge.packaging

class PackagingImpactClassifier {

    fun classify(
        packaging: Packaging?
    ): PackagingImpactLevel {

        if (packaging == null) {

            return PackagingImpactLevel.MEDIUM

        }

        return when {

            packaging.score < 20 ->
                PackagingImpactLevel.VERY_LOW

            packaging.score < 40 ->
                PackagingImpactLevel.LOW

            packaging.score < 60 ->
                PackagingImpactLevel.MEDIUM

            packaging.score < 80 ->
                PackagingImpactLevel.HIGH

            else ->
                PackagingImpactLevel.VERY_HIGH

        }

    }

}