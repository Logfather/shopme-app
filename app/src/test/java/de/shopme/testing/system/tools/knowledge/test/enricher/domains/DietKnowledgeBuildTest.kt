package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.DietCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.DietKnowledgeWriter
import de.shopme.tools.knowledge.diet.DefaultDietResolver
import de.shopme.tools.knowledge.diet.StringDietLoader
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class DietKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesDietKnowledge() {

        val resolver =

            DefaultDietResolver(

                StringDietLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/diet_classification.json"

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

                    DietCompilerPass(

                        resolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            DietKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "data/generated/diet_classification.json"

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