package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionCompilerPass
import de.shopme.tools.knowledge.compiler.writer.NutritionKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.nutrition.DefaultNutritionFactsResolver
import de.shopme.tools.knowledge.nutrition.StringNutritionFactsLoader
import org.junit.Test
import kotlin.test.assertNotNull

class NutritionKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesNutritionKnowledge() {

        val factsResolver =

            DefaultNutritionFactsResolver(

                StringNutritionFactsLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/nutrition_facts.json"

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

                    NutritionCompilerPass(

                        factsResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            NutritionKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/nutrition.json"

            )

        assert(output.exists())

        assert(output.length() > 0)

        val knowledge =

            writer.knowledge()

        assert(

            knowledge.entries.isNotEmpty()

        )

        assertNotNull(

            knowledge.entries["apple"]

        )

        assert(

            knowledge.entries["apple"]!!.calories == 52.0

        )

    }

}