package de.shopme.presentation.developer.report

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.off.OFFGapMatchReport

class KnowledgeBaselineReportBuilder {

    fun build(

        catalog: List<CatalogItem>,

        matchReport: OFFGapMatchReport

    ): KnowledgeBaselineReport {

        return KnowledgeBaselineReport(

            catalogItems =
                catalog.size,

            totalGaps =
                matchReport.totalGaps,

            matchedFoods =
                matchReport.matchedFoods,

            unmatchedFoods =
                matchReport.unmatchedFoods,

            dimensionCoverage =
                matchReport.dimensionCoverage
                    .mapKeys {
                        it.key.name
                    }
        )
    }
}