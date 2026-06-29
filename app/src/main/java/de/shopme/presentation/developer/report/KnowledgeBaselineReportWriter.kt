package de.shopme.presentation.developer.report

import com.google.gson.GsonBuilder
import java.io.File

class KnowledgeBaselineReportWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        report: KnowledgeBaselineReport,

        output: File

    ) {

        output.parentFile.mkdirs()

        output.writeText(

            gson.toJson(report)

        )
    }
}