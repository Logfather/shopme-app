package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.IngredientsCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.IngredientsKnowledgeWriter
import de.shopme.tools.knowledge.ingredients.DefaultIngredientsResolver
import de.shopme.tools.knowledge.ingredients.StringIngredientsLoader
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class IngredientsKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesIngredientsKnowledge() {

        val resolver =

            DefaultIngredientsResolver(

                StringIngredientsLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/data/v1/ingredients.json"

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

                    IngredientsCompilerPass(

                        resolver

                    )

                )

                .build()

        val writer =

            IngredientsKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/ingredients.json"

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

                "pizza margherita"

            )

        )

        assert(

            knowledge.entries["pizza margherita"]!!

                .contains(

                    "MOZZARELLA"

                )

        )

    }

}