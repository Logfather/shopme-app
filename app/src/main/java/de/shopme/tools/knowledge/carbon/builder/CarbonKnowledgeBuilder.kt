package de.shopme.tools.knowledge.carbon.builder

import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.importer.CarbonSourceImporter
import de.shopme.tools.knowledge.carbon.mapper.CarbonCandidateMapper
import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMergeReportBuilder
import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMerger
import de.shopme.tools.knowledge.carbon.merge.CarbonMergeReportPrinter
import de.shopme.tools.knowledge.carbon.validation.CarbonConflictReportPrinter
import de.shopme.tools.knowledge.carbon.validation.CarbonConflictValidator

class CarbonKnowledgeBuilder(

    private val importers: List<CarbonSourceImporter>,

    private val merger: CarbonCandidateMerger,

    private val mapper: CarbonCandidateMapper,

    private val reportBuilder: CarbonCandidateMergeReportBuilder =
        CarbonCandidateMergeReportBuilder(),

    private val reportPrinter: CarbonMergeReportPrinter? = null

) {

    fun build():
            Map<String, CarbonFootprint> {

        val candidates =
            importers.flatMap { importer ->

                importer.load()

            }

        val mergedCandidates =
            merger.merge(
                candidates
            )

        val report =
            reportBuilder.build(
                candidates = candidates,
                merged = mergedCandidates
            )

        val validationReport =
            CarbonConflictValidator()
                .validate(
                    report
                )

        CarbonConflictReportPrinter()
            .print(
                validationReport
            )

        reportPrinter?.print(
            report
        )

        return mergedCandidates.mapValues { (_, candidate) ->

            mapper.map(
                candidate
            )
        }
    }
}