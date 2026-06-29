package de.shopme.tools.knowledge.importer.off

import de.shopme.tools.knowledge.importer.KnowledgeImportSource
import java.io.File

class OFFImportSource(
    private val reader: JsonlGzipOFFImportReader,
    private val file: File
) : KnowledgeImportSource<OFFProduct> {

    override fun read(): Sequence<OFFProduct> {
        return reader.read(file)
    }
}