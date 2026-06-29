package de.shopme.tools.knowledge.off

data class OFFMinimalProductSource(

    val code: String?,

    val productName: String?,

    val genericName: String?,

    val brands: String?,

    val categoriesTags: List<String>,

    val ingredientsText: String?,

    val allergensTags: List<String>,

    val nutrimentsJson: String?,

    val novaGroup: String?,

    val ecoScoreGrade: String?,

    val labelsTags: List<String>,

    val countriesTags: List<String>
)