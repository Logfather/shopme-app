package de.shopme.testing.system.tools.knowledge.catalog

import de.shopme.tools.knowledge.catalog.CatalogNutritionReferenceNormalizer
import de.shopme.tools.knowledge.catalog.report.CatalogNutritionNormalizationReportBuilder
import de.shopme.tools.knowledge.catalog.report.CatalogNutritionNormalizationReportPrinter
import org.junit.Test
import java.io.File

class CatalogNutritionReferenceNormalizerTest {

    @Test
    fun normalizeCatalog() {

        val outputFile =
            File(
                "build/generated/normalized_supermarket_dataset.json"
            )

        CatalogNutritionReferenceNormalizer()
            .normalize(
                inputFile =
                    File(
                        "src/main/assets/catalog/supermarket_dataset.json"
                    ),

                outputFile =
                    outputFile
            )

        val normalizedContent =
            outputFile.readText()

        val report =
            CatalogNutritionNormalizationReportBuilder()
                .build(
                    normalizedContent
                )

        CatalogNutritionNormalizationReportPrinter()
            .print(
                report
            )
    }
}