package de.shopme.tools.knowledge.carbon.importer

import de.shopme.tools.knowledge.agribalyse.loader.AgribalyseSyntheseCsvLoader
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseCarbonCandidateMapper
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseSyntheseRowMapper
import de.shopme.tools.knowledge.agribalyse.report.AgribalyseMappingStatistics
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import java.io.File

class AgribalyseCarbonImporter(

    private val file: File,

    private val productColumn: String,

    private val carbonColumn: String,

    private val csvLoader: AgribalyseSyntheseCsvLoader =
        AgribalyseSyntheseCsvLoader(),

    private val rowMapper: AgribalyseSyntheseRowMapper =
        AgribalyseSyntheseRowMapper(
            productColumn = productColumn,
            carbonColumn = carbonColumn
        ),

    private val candidateMapper: AgribalyseCarbonCandidateMapper =
        AgribalyseCarbonCandidateMapper()

) : CarbonSourceImporter {

    var statistics =
        AgribalyseMappingStatistics(
            totalRows = 0,
            validRows = 0,
            mappedRows = 0,
            mappedReferences = emptyMap(),
            unmappedReferences = emptyMap()
        )
        private set

    override fun load():
            List<CarbonKnowledgeCandidate> {

        val rows =
            csvLoader.load(
                file
            )

        val mappedReferences =
            mutableMapOf<String, Int>()

        val unmappedReferences =
            mutableMapOf<String, Int>()

        val validRows =
            rows
                .map { csvRow ->

                    rowMapper.map(
                        csvRow
                    )
                }
                .filter { row ->

                    row.productName.isNotBlank() &&
                            row.climateChangeKgCo2ePerKg > 0.0
                }

        val candidates =
            validRows.map { row ->

                val candidate =
                    candidateMapper.map(
                        row
                    )

                if (candidateMapper.wasLastReferenceMapped()) {

                    mappedReferences[candidate.reference] =
                        mappedReferences.getOrDefault(
                            candidate.reference,
                            0
                        ) + 1

                } else {

                    unmappedReferences[candidate.reference] =
                        unmappedReferences.getOrDefault(
                            candidate.reference,
                            0
                        ) + 1
                }

                candidate
            }

        statistics =
            AgribalyseMappingStatistics(
                totalRows = rows.size,
                validRows = validRows.size,
                mappedRows = mappedReferences.values.sum(),
                mappedReferences = mappedReferences,
                unmappedReferences = unmappedReferences
            )

        return candidates
    }
}