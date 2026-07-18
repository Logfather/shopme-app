package de.shopme.tools.knowledge.mapping.catalog.report

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class CatalogServerMappingCoverageReportWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
) {

    fun write(
        report: CatalogServerMappingCoverageReport,
        file: File
    ) {

        val parentDirectory =
            requireNotNull(
                file.parentFile
            ) {
                "Report file has no parent directory: " +
                        file.absolutePath
            }

        if (!parentDirectory.exists()) {
            check(parentDirectory.mkdirs()) {
                "Could not create report directory: " +
                        parentDirectory.absolutePath
            }
        }

        val temporaryFile =
            File(
                parentDirectory,
                "${file.name}.tmp"
            )

        temporaryFile.writeText(
            gson.toJson(report)
        )

        try {
            Files.move(
                temporaryFile.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (
            exception: AtomicMoveNotSupportedException
        ) {
            Files.move(
                temporaryFile.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}