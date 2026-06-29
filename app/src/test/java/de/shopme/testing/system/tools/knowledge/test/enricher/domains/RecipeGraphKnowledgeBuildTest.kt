package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.RecipeGraphCompilerPass
import de.shopme.tools.knowledge.compiler.writer.RecipeGraphKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.recipegraph.DefaultRecipeGraphResolver
import de.shopme.tools.knowledge.recipegraph.StringRecipeGraphLoader
import org.junit.Test

class RecipeGraphKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesRecipeGraphKnowledge() {

        val resolver =

            DefaultRecipeGraphResolver(

                StringRecipeGraphLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/recipe_graph.json"

                    )

                ).load()

            )

        val compiler =

            TestFoodKnowledgeCompilerBuilder()

                .add(

                    NutritionAliasCompilerPass(

                        aliasResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .add(

                    RecipeGraphCompilerPass(

                        resolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            RecipeGraphKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/recipe_graph.json"

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

        println(knowledge.entries.keys)

        assert(

            knowledge.entries.containsKey(

                "lasagna"

            )

        )

        assert(

            knowledge.entries["lasagna"]!!

                .ingredients.contains(

                    "tomato"

                )

        )

        assert(

            knowledge.entries["lasagna"]!!

                .ingredients.contains(

                    "mozzarella"

                )

        )

        assert(

            knowledge.entries["lasagna"]!!

                .ingredients.contains(

                    "beef"

                )

        )

    }

}