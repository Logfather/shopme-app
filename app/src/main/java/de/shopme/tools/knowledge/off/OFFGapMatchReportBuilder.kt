package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.gap.CatalogKnowledgeGap

class OFFGapMatchReportBuilder {

    fun build(

        gaps: List<CatalogKnowledgeGap>,

        matches: List<OFFGapMatch>

    ): OFFGapMatchReport {

        val dimensionCoverage =
            mutableMapOf<KnowledgeDimensionId, Int>()

        matches.forEach { match ->

            match.matchedDimensions.forEach { dimension ->

                dimensionCoverage[dimension] =
                    (dimensionCoverage[dimension] ?: 0) + 1
            }
        }

        return OFFGapMatchReport(

            totalGaps = gaps.size,

            matchedFoods = matches.size,

            unmatchedFoods =
                (gaps.size - matches.size)
                    .coerceAtLeast(0),

            dimensionCoverage =
                dimensionCoverage.toSortedMap(
                    compareBy { it.name }
                ),

            matches = matches
        )
    }
}