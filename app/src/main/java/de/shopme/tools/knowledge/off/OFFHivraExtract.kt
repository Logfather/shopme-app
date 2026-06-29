package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

data class OFFHivraExtract(

    val carbon: OFFCarbonExtract? = null,

    val code: String?,

    val productName: String,

    val productNameDe: String?,

    val productNameEn: String?,

    val productNameFr: String?,

    val brands: String?,

    val countriesTags: List<String>,

    val languagesTags: List<String>,

    val categoriesTags: List<String>,

    val categoriesHierarchy: List<String>,

    val ingredientsText: String?,

    val ingredientsTags: List<String>,

    val allergensTags: List<String>,

    val tracesTags: List<String>,

    val nutriments: Map<String, Double?>,

    val nutriscoreGrade: String?,

    val novaGroup: Int?,

    val packagingMaterialsTags: List<String>,

    val packagingShapesTags: List<String>,

    val originsTags: List<String>,

    val manufacturingPlaces: String?,

    val labelsTags: List<String>,

    val ecoscoreGrade: String?,

    val environmentalScoreGrade: String?,

    val availableDimensions: Set<KnowledgeDimensionId>,

    val missingDimensions: Set<KnowledgeDimensionId>
)