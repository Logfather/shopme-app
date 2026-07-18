package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeMappingPersistenceStep
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildPersistenceResult
import java.io.File

class DefaultNutritionKnowledgeMappingPersistenceStep(
    private val outputMappingFile: File,
    private val representativeValidationFile: File,
    private val persistMappings: () -> Unit,
    private val validateProductiveLowConfidenceMatches:
    (() -> Unit)? =
        null,
    private val representativeMappingMerger:
    RepresentativeNutritionMappingMerger =
        RepresentativeNutritionMappingMerger()
) : NutritionKnowledgeMappingPersistenceStep {

    override fun run():
            NutritionKnowledgeRebuildPersistenceResult {

        val beforeCount =
            countMappings(
                file =
                    outputMappingFile
            )

        /*
         * Schreibt die aktuell akzeptierten regulären Decisions.
         *
         * Dieser Schritt darf die vorhandene Datei vollständig
         * ersetzen. Unmittelbar danach werden die persistierten
         * Representative-Mappings wieder deterministisch ergänzt.
         */
        persistMappings()

        require(outputMappingFile.isFile) {
            "Catalog-server mapping file was not created: " +
                    outputMappingFile.absolutePath
        }

        val regularMappingCount =
            countMappings(
                file =
                    outputMappingFile
            )

        /*
         * Produktive MATCH-Decisions unterhalb der regulären
         * Persistenzschwelle werden deterministisch als mögliche
         * Representative-Mappings nachvalidiert.
         *
         * Im OFFLINE-Modus ist diese Funktion nicht konfiguriert.
         */
        validateProductiveLowConfidenceMatches
            ?.invoke()

        val representativeResult =
            representativeMappingMerger.merge(
                representativeValidationFile =
                    representativeValidationFile,
                mappingFile =
                    outputMappingFile
            )

        val finalCount =
            countMappings(
                file =
                    outputMappingFile
            )

        require(
            representativeResult.existingMappingCount ==
                    regularMappingCount
        ) {
            "Representative merger read a different regular mapping count."
        }

        require(
            finalCount ==
                    representativeResult.finalMappingCount
        ) {
            "Persisted mapping count differs from representative " +
                    "merge result: persisted=$finalCount, " +
                    "merge=${representativeResult.finalMappingCount}."
        }

        require(finalCount >= beforeCount) {
            "Nutrition mapping persistence removed mappings: " +
                    "before=$beforeCount, after=$finalCount."
        }

        val addedCount =
            finalCount -
                    beforeCount

        val unchangedCount =
            finalCount -
                    addedCount

        return NutritionKnowledgeRebuildPersistenceResult(
            existingMappingCount =
                beforeCount,
            addedMappingCount =
                addedCount,
            unchangedMappingCount =
                unchangedCount,
            conflictCount =
                0,
            finalMappingCount =
                finalCount
        )
    }

    private fun countMappings(
        file: File
    ): Int {

        if (!file.isFile) {
            return 0
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Catalog-server mapping file must contain a JSON " +
                    "object: " +
                    file.absolutePath
        }

        val mappings =
            root.asJsonObject["mappings"]
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: error(
                    "Catalog-server mapping file contains no " +
                            "'mappings' array: " +
                            file.absolutePath
                )

        return mappings.size()
    }
}