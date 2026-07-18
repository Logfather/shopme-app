package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMatchDecisionValidationResult(
    val mappings: CatalogServerKnowledgeMappings,
    val report: CatalogKnowledgeMappingValidationReport
)