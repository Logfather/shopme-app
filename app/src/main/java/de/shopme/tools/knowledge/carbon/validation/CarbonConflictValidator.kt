package de.shopme.tools.knowledge.carbon.validation

import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMergeReport
import de.shopme.tools.knowledge.source.KnowledgeValidator
import kotlin.math.abs

class CarbonConflictValidator :

    KnowledgeValidator<
            CarbonCandidateMergeReport,
            CarbonConflictValidationReport
            > {

    override fun validate(
        report: CarbonCandidateMergeReport
    ): CarbonConflictValidationReport {

        val warnings =
            report.conflicts
                .mapNotNull { conflict ->

                    val values =
                        conflict.candidates
                            .map {
                                it.kgCo2ePerKg
                            }

                    val min =
                        values.minOrNull()
                            ?: return@mapNotNull null

                    val max =
                        values.maxOrNull()
                            ?: return@mapNotNull null

                    if (min <= 0.0) {
                        return@mapNotNull null
                    }

                    val differencePercent =
                        abs(max - min) / min * 100.0

                    if (differencePercent < 5.0) {
                        return@mapNotNull null
                    }

                    CarbonConflictWarning(
                        reference =
                            conflict.reference,
                        minKgCo2ePerKg =
                            min,
                        maxKgCo2ePerKg =
                            max,
                        differencePercent =
                            differencePercent
                    )
                }

        return CarbonConflictValidationReport(
            warnings = warnings
        )
    }
}