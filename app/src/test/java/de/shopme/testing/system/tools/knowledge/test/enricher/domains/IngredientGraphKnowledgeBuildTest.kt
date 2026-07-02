package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.IngredientGraphCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.IngredientGraphKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.ingredientgraph.DefaultIngredientGraphResolver
import de.shopme.tools.knowledge.ingredientgraph.StringIngredientGraphLoader
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class IngredientGraphKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesIngredientGraphKnowledge() {

        val resolver =

            DefaultIngredientGraphResolver(

                StringIngredientGraphLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/ingredient_graph.json"

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

                    IngredientGraphCompilerPass(

                        resolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            IngredientGraphKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "data/generated/ingredient_graph.json"

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

        assert(

            knowledge.entries.containsKey(

                "remoulade"

            )

        )

        assert(

            knowledge.entries["remoulade"]!!

                .ingredients.contains(

                    "egg"

                )

        )

        assert(

            knowledge.entries["remoulade"]!!

                .ingredients.contains(

                    "mustard"

                )

        )

    }

}