package de.shopme.tools.knowledge.gap

import com.google.gson.GsonBuilder
import java.io.File

class CatalogKnowledgeGapExporter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun export(

        gaps: List<CatalogKnowledgeGap>,

        output: File

    ) {

        output.parentFile.mkdirs()

        output.writeText(

            gson.toJson(

                CatalogKnowledgeGapExport(

                    entries = gaps.map { gap ->

                        CatalogKnowledgeGapExportEntry(

                            food = gap.normalizedName,

                            missing = gap
                                .missingDimensions
                                .map {
                                    it.name.lowercase()
                                }
                                .sorted()
                        )
                    }
                )
            )
        )
    }
}