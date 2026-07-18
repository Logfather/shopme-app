package de.shopme.testing.system.tools.knowledge.report

import com.google.gson.GsonBuilder
import java.io.File

class CatalogServerKnowledgeMatchReportWriter {

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()

    fun write(
        report: CatalogServerKnowledgeMatchReport,
        outputFile: File
    ) {
        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(report)
        )
    }
}