package de.shopme.tools.knowledge.importer.off

import java.io.File

interface OFFImportReader {

    fun read(
        file: File
    ): Sequence<OFFProduct>
}