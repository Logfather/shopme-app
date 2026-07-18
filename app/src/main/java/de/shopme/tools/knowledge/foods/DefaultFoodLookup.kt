package de.shopme.tools.knowledge.foods

import de.shopme.domain.food.GlycemicIndexLevel
import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfare
import de.shopme.tools.knowledge.biodiversity.BiodiversityScore
import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.CarbonImpactLevel
import de.shopme.tools.knowledge.diet.DietClassification
import de.shopme.tools.knowledge.fairtrade.FairTrade
import de.shopme.tools.knowledge.foodmiles.FoodMiles
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphEntry
import de.shopme.tools.knowledge.locality.Locality
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.packaging.Packaging
import de.shopme.tools.knowledge.pesticides.Pesticide
import de.shopme.tools.knowledge.pollinator.PollinatorScore
import de.shopme.tools.knowledge.processing.ProcessingLevel
import de.shopme.tools.knowledge.production.ProductionMethod
import de.shopme.tools.knowledge.recipegraph.RecipeGraphEntry
import de.shopme.tools.knowledge.waterfootprint.WaterFootprint
import de.shopme.tools.knowledge.waterstress.WaterStress

class DefaultFoodLookup(

    knowledge: FoodsKnowledge

) : FoodLookup {

    private val foodsById =
        knowledge.foods.associateBy {
            it.id
        }

    private val foodsByCanonicalName =
        knowledge.foods.associateBy {
            it.names.canonical
        }

    override fun findById(
        id: String
    ): FoodKnowledgeSourceEntry? =
        foodsById[id]

    override fun findByCanonicalName(
        name: String
    ): FoodKnowledgeSourceEntry? =
        foodsByCanonicalName[name]

    override fun nutritionReference(
        canonicalName: String
    ): String? =
        findByCanonicalName(canonicalName)
            ?.knowledge
            ?.nutrition
            ?.reference

    override fun carbonReference(
        canonicalName: String
    ): String? =
        findByCanonicalName(canonicalName)
            ?.knowledge
            ?.carbon
            ?.reference

    override fun nutritionFacts(
        canonicalName: String
    ): NutritionFacts? =
        findByCanonicalName(canonicalName)
            ?.knowledge
            ?.nutrition
            ?.value

    override fun carbonFootprint(
        canonicalName: String
    ): CarbonFootprint? =

        findByCanonicalName(

            canonicalName

        )
            ?.knowledge
            ?.carbon
            ?.value

    override fun waterFootprint(
        canonicalName: String
    ): WaterFootprint? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.water
            ?.value

    override fun waterStress(
        canonicalName: String
    ): WaterStress? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.waterStress
            ?.value

    override fun biodiversity(
        canonicalName: String
    ): BiodiversityScore? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.biodiversity
            ?.value

    override fun pollinator(
        canonicalName: String
    ): PollinatorScore? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.pollinator
            ?.value

    override fun pesticide(
        canonicalName: String
    ): Pesticide? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.pesticide
            ?.value

    override fun production(
        canonicalName: String
    ): Set<ProductionMethod>? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.production
            ?.value

    override fun processing(
        canonicalName: String
    ): ProcessingLevel? =
        findByCanonicalName(canonicalName)
            ?.knowledge
            ?.processing
            ?.value

    override fun packaging(
        canonicalName: String
    ): Packaging? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.packaging
            ?.value

    override fun locality(
        canonicalName: String
    ): Locality? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.locality
            ?.value

    override fun fairTrade(
        canonicalName: String
    ): FairTrade? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.fairTrade
            ?.value

    override fun foodMiles(
        canonicalName: String
    ): FoodMiles? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.foodMiles
            ?.value

    override fun animalWelfare(
        canonicalName: String
    ): AnimalWelfare? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.animalWelfare
            ?.value

    override fun ingredients(
        canonicalName: String
    ): Set<String>? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.ingredients
            ?.value

    override fun allergens(
        canonicalName: String
    ): Set<Allergen>? =
        findByCanonicalName(canonicalName)
            ?.knowledge
            ?.allergens
            ?.value

    override fun taxonomy(
        canonicalName: String
    ): List<String>? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.taxonomy
            ?.value

    override fun seasonality(
        canonicalName: String
    ): List<Int>? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.seasonality
            ?.value

    override fun dietClassifications(
        canonicalName: String
    ): Set<DietClassification>? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.dietClassifications
            ?.value

    override fun nutriScore(
        canonicalName: String
    ): NutriScore? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.nutriScore
            ?.value

    override fun carbonImpact(
        canonicalName: String
    ): CarbonImpactLevel? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.carbonImpact
            ?.value

    override fun glycemicIndex(
        canonicalName: String
    ): GlycemicIndexLevel? =
        findByCanonicalName(canonicalName)
            ?.knowledge
            ?.glycemicIndex
            ?.value

    override fun ingredientGraph(
        canonicalName: String
    ): IngredientGraphEntry? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.ingredientGraph
            ?.value

    override fun recipeGraph(
        canonicalName: String
    ): RecipeGraphEntry? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.recipeGraph
            ?.value

    override fun recipes(
        canonicalName: String
    ): Set<String>? =

        findByCanonicalName(
            canonicalName
        )
            ?.knowledge
            ?.recipes
            ?.value


}