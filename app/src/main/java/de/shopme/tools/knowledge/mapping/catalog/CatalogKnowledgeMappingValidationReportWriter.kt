package de.shopme.tools.knowledge.mapping.catalog

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class CatalogKnowledgeMappingValidationReportWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
) {

    fun write(
        report: CatalogKnowledgeMappingValidationReport,
        file: File
    ) {

        val parent =
            requireNotNull(
                file.parentFile
            ) {
                "Validation report file has no parent directory: " +
                        file.absolutePath
            }

        if (!parent.exists()) {
            check(parent.mkdirs()) {
                "Could not create validation report directory: " +
                        parent.absolutePath
            }
        }

        val temporaryFile =
            File(
                parent,
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