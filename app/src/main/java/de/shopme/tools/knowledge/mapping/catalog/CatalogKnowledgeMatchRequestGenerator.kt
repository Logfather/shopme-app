package de.shopme.tools.knowledge.mapping.catalog

import java.io.File

interface CatalogKnowledgeMatchRequestGenerator {

    fun generate(
        matchReportFile: File
    ): CatalogKnowledgeMatchRequests
}