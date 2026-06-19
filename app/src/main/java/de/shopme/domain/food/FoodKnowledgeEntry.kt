package de.shopme.domain.food

import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfare
import de.shopme.tools.knowledge.biodiversity.BiodiversityScore
import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.CarbonImpactLevel
import de.shopme.tools.knowledge.diet.DietClassification
import de.shopme.tools.knowledge.fairtrade.FairTrade
import de.shopme.tools.knowledge.foodmiles.FoodMiles
import de.shopme.tools.knowledge.locality.Locality
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.packaging.Packaging
import de.shopme.tools.knowledge.pesticide.Pesticide
import de.shopme.tools.knowledge.pollinator.PollinatorScore
import de.shopme.tools.knowledge.processing.ProcessingLevel
import de.shopme.tools.knowledge.production.ProductionMethod
import de.shopme.tools.knowledge.waterfootprint.WaterFootprint
import de.shopme.tools.knowledge.waterstress.WaterStress

data class FoodKnowledgeEntry(

    val inputName: String,

    val normalizedName: String,

    val category: FoodCategory,

    val tags: Set<FoodTag>,

    val nutritionReference: String?,

    val glycemicIndex: GlycemicIndexLevel,

    val allergens: Set<Allergen>,

    val dietClassifications: Set<DietClassification>,

    val production: Set<ProductionMethod>,

    val seasonality: List<Int>,

    val carbonFootprint: CarbonFootprint?,

    val ingredients: Set<String>,

    val recipes: Set<String>,

    val taxonomyPath: List<String>,

    val waterFootprint: WaterFootprint?,

    val biodiversity: BiodiversityScore?,

    val pollinator: PollinatorScore?,

    val foodMiles: FoodMiles?,

    val packaging: Packaging?,

    val processing: ProcessingLevel?,

    val locality: Locality?,

    val animalWelfare: AnimalWelfare?,

    val fairTrade: FairTrade?,

    val pesticide: Pesticide?,

    val waterStress: WaterStress?,

    val nutriScore: NutriScore?,

    val carbonImpact: CarbonImpactLevel?,

    val nutritionFacts: NutritionFacts?

)