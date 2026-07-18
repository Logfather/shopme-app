package de.shopme.tools.knowledge.mapping.catalog

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class CatalogServerKnowledgeMappingWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
) {

    fun write(
        mappings: CatalogServerKnowledgeMappings,
        file: File
    ) {

        ensureParentDirectory(
            file = file
        )

        val temporaryFile =
            File(
                file.parentFile,
                "${file.name}.tmp"
            )

        temporaryFile.writeText(
            gson.toJson(mappings)
        )

        moveAtomically(
            source = temporaryFile,
            target = file
        )
    }


    private fun ensureParentDirectory(
        file: File
    ) {

        val parent =
            requireNotNull(
                file.parentFile
            ) {
                "Mapping output file has no parent directory: " +
                        file.absolutePath
            }

        if (!parent.exists()) {
            check(parent.mkdirs()) {
                "Could not create mapping output directory: " +
                        parent.absolutePath
            }
        }
    }


    private fun moveAtomically(
        source: File,
        target: File
    ) {

        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (
            exception: AtomicMoveNotSupportedException
        ) {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}