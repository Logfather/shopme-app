package de.shopme.tools.knowledge.compiler.candidate

import java.io.File

interface KnowledgeImportReader {

    fun read(file: File): KnowledgeImportBatch
}