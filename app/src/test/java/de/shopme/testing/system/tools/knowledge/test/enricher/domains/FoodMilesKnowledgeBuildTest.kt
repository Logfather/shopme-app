package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.FoodMilesCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.FoodMilesKnowledgeWriter
import de.shopme.tools.knowledge.foodmiles.DefaultFoodMilesResolver
import de.shopme.tools.knowledge.foodmiles.StringFoodMilesLoader
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class FoodMilesKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesFoodMilesKnowledge() {

        val foodMilesResolver =

            DefaultFoodMilesResolver(

                StringFoodMilesLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/data/v1/food_miles.json"

                    )

                ).load()

            )

        val compiler =

            TestFoodKnowledgeCompilerBuilder()

                .add(

                    NutritionAliasCompilerPass(

                        aliasResolver

                    )

                )

                .add(

                    FoodMilesCompilerPass(

                        foodMilesResolver

                    )

                )

                .build()

        val writer =

            FoodMilesKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/food_miles.json"

            )

        assert(

            output.exists()

        )

        assert(

            output.length() > 0

        )

        val knowledge =

            writer.knowledge()

        assert(

            knowledge.entries.isNotEmpty()

        )

    }

}