package de.shopme.tools.knowledge.mapping.catalog

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class CatalogServerKnowledgeMappingMergeReportWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
) {

    fun writeConflictReport(
        report: CatalogServerKnowledgeMappingConflictReport,
        file: File
    ) {
        writeAtomically(
            value = report,
            file = file
        )
    }


    fun writeMergeReport(
        report: CatalogServerKnowledgeMappingMergeReport,
        file: File
    ) {
        writeAtomically(
            value = report,
            file = file
        )
    }


    private fun writeAtomically(
        value: Any,
        file: File
    ) {

        val parentDirectory =
            requireNotNull(
                file.parentFile
            ) {
                "Output file has no parent directory: " +
                        file.absolutePath
            }

        if (!parentDirectory.exists()) {
            check(parentDirectory.mkdirs()) {
                "Could not create output directory: " +
                        parentDirectory.absolutePath
            }
        }

        val temporaryFile =
            File(
                parentDirectory,
                "${file.name}.tmp"
            )

        temporaryFile.writeText(
            gson.toJson(value)
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