package de.shopme.tools.knowledge.agribalyse.extractor

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import java.io.File

class AgribalyseCandidateExtractor {

    fun extract(
        file: File,
        maxCandidates: Int? = null
    ): List<CanonicalKnowledgeCandidate> {

        if (!file.exists()) {
            error("Agribalyse slim TSV missing: ${file.absolutePath}")
        }

        val lines =
            file.useLines { sequence ->
                sequence.toList()
            }

        if (lines.isEmpty()) {
            return emptyList()
        }

        val header =
            lines.first()
                .split("\t")
                .mapIndexed { index, name ->
                    name to index
                }
                .toMap()

        return lines
            .drop(1)
            .asSequence()
            .mapNotNull { line ->
                parseLine(
                    line = line,
                    header = header
                )
            }
            .let { sequence ->
                if (maxCandidates != null) {
                    sequence.take(maxCandidates)
                } else {
                    sequence
                }
            }
            .toList()
    }

    private fun parseLine(
        line: String,
        header: Map<String, Int>
    ): CanonicalKnowledgeCandidate? {

        val columns =
            line.split("\t")

        val nameFr =
            columns.valueAt(header, "name_fr")
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?: return null

        val nameEn =
            columns.valueAt(header, "name_en")
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }

        val codeAgb =
            columns.valueAt(header, "code_agb")
                ?.trim()
                .orEmpty()

        val codeCiqual =
            columns.valueAt(header, "code_ciqual")
                ?.trim()
                .orEmpty()

        val payload =
            mutableMapOf<String, Double>()

        columns.doubleAt(header, "environment_score_mpt_per_kg")
            ?.let { payload["environmentScoreMptPerKg"] = it }

        columns.doubleAt(header, "climate_total_kg_co2_eq_per_kg")
            ?.let { payload["climateKgCo2EqPerKg"] = it }

        columns.doubleAt(header, "land_use_pt_per_kg")
            ?.let { payload["landUsePtPerKg"] = it }

        columns.doubleAt(header, "water_deprivation_m3_per_kg")
            ?.let { payload["waterDeprivationM3PerKg"] = it }

        val waterPayload =
            columns.doubleAt(header, "water_deprivation_m3_per_kg")
                ?.times(1000.0)
                ?.takeIf { it > 0.0 }
                ?.let { litersPerKilogram ->
                    linkedMapOf<String, Any>(
                        "litersPerKilogram" to litersPerKilogram
                    )
                }

        val waterStressPayload =
            columns.doubleAt(
                header,
                "water_deprivation_m3_per_kg"
            )
                ?.takeIf {
                    it > 0.0
                }
                ?.let { score ->
                    linkedMapOf<String, Any>(
                        "score" to score
                    )
                }

        val biodiversityPayload =
            columns.doubleAt(
                header,
                "land_use_pt_per_kg"
            )
                ?.times(100.0)
                ?.coerceIn(0.0, 100.0)
                ?.let { score ->
                    linkedMapOf<String, Any>(
                        "score" to score
                    )
                }

        val pollinatorPayload =
            columns.doubleAt(
                header,
                "land_use_pt_per_kg"
            )
                ?.times(100.0)
                ?.coerceIn(0.0, 100.0)
                ?.let { score ->
                    linkedMapOf<String, Any>(
                        "score" to score
                    )
                }

        val pesticidesPayload =
            columns.doubleAt(
                header,
                "land_use_pt_per_kg"
            )
                ?.coerceIn(0.0, 1.0)
                ?.let { score ->
                    linkedMapOf<String, Any>(
                        "score" to score
                    )
                }

        val dimensions =
            listOfNotNull(
                payload
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        KnowledgeDimensionCandidate(
                            dimension =
                                KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT,
                            payload = it
                        )
                    },
                waterPayload
                    ?.let {
                        KnowledgeDimensionCandidate(
                            dimension =
                                KnowledgeDimensionCandidateType.WATER,
                            payload = it
                        )
                    },
                waterStressPayload
                    ?.let {
                        KnowledgeDimensionCandidate(
                            dimension =
                                KnowledgeDimensionCandidateType.WATER_STRESS,
                            payload = it
                        )
                    },
                biodiversityPayload
                    ?.let {
                        KnowledgeDimensionCandidate(
                            dimension =
                                KnowledgeDimensionCandidateType.BIODIVERSITY,
                            payload = it
                        )
                    },
                pollinatorPayload
                    ?.let {
                        KnowledgeDimensionCandidate(
                            dimension =
                                KnowledgeDimensionCandidateType.POLLINATOR,
                            payload = it
                        )
                    },
                pesticidesPayload
                    ?.let {
                        KnowledgeDimensionCandidate(
                            dimension =
                                KnowledgeDimensionCandidateType.PESTICIDES,
                            payload = it
                        )
                    }
            )

        if (dimensions.isEmpty()) {
            return null
        }

        val aliases =
            buildSet {
                add(nameFr)

                if (nameEn != null) {
                    add(nameEn)
                }
            }

        return CanonicalKnowledgeCandidate(
            canonicalId = nameFr,
            aliases = aliases,
            dimensions = dimensions,
            metadata = CandidateMetadata(
                source = "agribalyse",
                sourceId = codeAgb.ifBlank { codeCiqual.ifBlank { nameFr } },
                confidence = 1.0,
                version = "3.2",
                attributes = mapOf(
                    "codeAgb" to codeAgb,
                    "codeCiqual" to codeCiqual,
                    "nameFr" to nameFr,
                    "nameEn" to nameEn.orEmpty(),
                    "foodGroup" to columns.valueAt(header, "food_group").orEmpty(),
                    "foodSubGroup" to columns.valueAt(header, "food_sub_group").orEmpty(),
                    "dataQualityScore" to columns.valueAt(header, "data_quality_score").orEmpty()
                )
            )
        )
    }

    private fun List<String>.valueAt(
        header: Map<String, Int>,
        name: String
    ): String? {
        val index =
            header[name] ?: return null

        return getOrNull(index)
    }

    private fun List<String>.doubleAt(
        header: Map<String, Int>,
        name: String
    ): Double? {
        return valueAt(header, name)
            ?.trim()
            ?.replace(",", ".")
            ?.toDoubleOrNull()
    }
}