package de.shopme.tools.knowledge.agribalyse.classifier

import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetType
import java.text.Normalizer

class AgribalyseSheetClassifier {

    fun classify(sheetName: String): AgribalyseSheetType {

        val normalized = Normalizer.normalize(sheetName, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .trim()

        return when {
            normalized == "notice" ->
                AgribalyseSheetType.NOTICE

            normalized.contains("environnement") &&
                    normalized.contains("calcul") ->
                AgribalyseSheetType.CALCULATION_ENVIRONMENT

            normalized == "synthese" ->
                AgribalyseSheetType.SYNTHESIS

            normalized.contains("detail") &&
                    normalized.contains("etape") ->
                AgribalyseSheetType.STAGE_DETAIL

            normalized.contains("detail") &&
                    normalized.contains("ingredient") ->
                AgribalyseSheetType.INGREDIENT_DETAIL

            normalized.contains("graph") &&
                    normalized.contains("etape") ->
                AgribalyseSheetType.GRAPH_STAGE

            normalized.contains("graph") &&
                    normalized.contains("ingredient") ->
                AgribalyseSheetType.GRAPH_INGREDIENT

            else ->
                AgribalyseSheetType.UNKNOWN
        }
    }
}