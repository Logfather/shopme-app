package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.RecipeCompilerPass
import de.shopme.tools.knowledge.compiler.writer.RecipeKnowledgeWriter
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.recipe.DefaultRecipeResolver
import de.shopme.tools.knowledge.recipe.StringRecipeLoader
import org.junit.Test

class RecipeKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesRecipeKnowledge() {

        val resolver =

            DefaultRecipeResolver(

                StringRecipeLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/data/v1/recipes.json"

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

                    RecipeCompilerPass(

                        resolver

                    )

                )

                .build()

        val writer =

            RecipeKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/recipes.json"

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