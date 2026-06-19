package de.shopme.testing.system.tools.knowledge.test.enricher

import de.shopme.testing.system.tools.knowledge.test.enricher.domains.AllergenKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.AnimalWelfareKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.BiodiversityKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.CarbonImpactKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.CarbonKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.DietKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.FairTradeKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.FoodMilesKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.FoodSemanticsKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.FoodTaxonomyKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.GlycemicKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.IngredientGraphKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.IngredientsKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.LocalityKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.NutriScoreKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.NutritionKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.PackagingKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.PesticidesKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.PollinatorKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.ProductionKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.RecipeGraphKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.SeasonalityKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.WaterKnowledgeBuildTest
import de.shopme.testing.system.tools.knowledge.test.enricher.domains.WaterStressKnowledgeBuildTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(

    Suite::class

)

@Suite.SuiteClasses(

    CompilerInfrastructureTest::class,
    AllergenKnowledgeBuildTest::class,
    AnimalWelfareKnowledgeBuildTest::class,
    BiodiversityKnowledgeBuildTest::class,
    CarbonKnowledgeBuildTest::class,
    CarbonImpactKnowledgeBuildTest::class,
    DietKnowledgeBuildTest::class,
    FairTradeKnowledgeBuildTest::class,
    FoodMilesKnowledgeBuildTest::class,
    FoodSemanticsKnowledgeBuildTest::class,
    FoodTaxonomyKnowledgeBuildTest::class,
    GlycemicKnowledgeBuildTest::class,
    IngredientsKnowledgeBuildTest::class,
    IngredientGraphKnowledgeBuildTest::class,
    LocalityKnowledgeBuildTest::class,
    NutritionKnowledgeBuildTest::class,
    NutriScoreKnowledgeBuildTest::class,
    PackagingKnowledgeBuildTest::class,
    PesticidesKnowledgeBuildTest::class,
    PollinatorKnowledgeBuildTest::class,
    ProductionKnowledgeBuildTest::class,
    RecipeGraphKnowledgeBuildTest::class,
    SeasonalityKnowledgeBuildTest::class,
    WaterKnowledgeBuildTest::class,
    WaterStressKnowledgeBuildTest::class,

)
class FoodKnowledgeBuildSuite