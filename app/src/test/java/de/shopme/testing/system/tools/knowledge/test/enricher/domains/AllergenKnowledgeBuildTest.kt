package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.allergen.DefaultAllergenResolver
import de.shopme.tools.knowledge.allergen.StringAllergenLoader
import de.shopme.tools.knowledge.compiler.passes.AllergenCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.AllergenKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class AllergenKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesAllergenKnowledge() {

        val resolver =

            DefaultAllergenResolver(

                StringAllergenLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/allergens.json"

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

                    AllergenCompilerPass(

                        resolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            AllergenKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "data/generated/allergens.json"

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