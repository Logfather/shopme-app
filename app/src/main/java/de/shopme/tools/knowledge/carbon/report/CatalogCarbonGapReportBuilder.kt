package de.shopme.tools.knowledge.carbon.report

class CatalogCarbonGapReportBuilder {

    fun build(
        catalogReferences: Set<String>,
        carbonReferences: Set<String>,
        agribalyseReferences: Set<String>
    ): CatalogCarbonGapReport {

        val missingCarbon =
            catalogReferences
                .filterNot { reference ->
                    reference in carbonReferences
                }
                .toSet()

        val agribalyseCandidates =
            missingCarbon
                .filter { reference ->
                    reference in agribalyseReferences
                }
                .sorted()

        return CatalogCarbonGapReport(
            catalogItems = catalogReferences.size,
            carbonCovered = catalogReferences.count { reference ->
                reference in carbonReferences
            },
            missingCarbon = missingCarbon.size,
            agribalyseCandidates = agribalyseCandidates
        )
    }
}