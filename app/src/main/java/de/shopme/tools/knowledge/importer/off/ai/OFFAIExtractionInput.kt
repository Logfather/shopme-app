package de.shopme.tools.knowledge.importer.off.ai

data class OFFAIExtractionInput(
    val code: String?,
    val productName: String?,
    val productNameDe: String?,
    val brands: String?,
    val categories: String?,
    val ingredientsText: String?,
    val ingredientsTextDe: String?,
    val labels: String?,
    val countries: String?,
    val quantity: String?
)