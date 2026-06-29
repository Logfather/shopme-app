package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.FoodSemanticsCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.TaxonomyCompilerPass
import de.shopme.tools.knowledge.compiler.writer.FoodSemanticsKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.semantics.DefaultFoodSemanticsResolver
import de.shopme.tools.knowledge.semantics.StringFoodSemanticsLoader
import de.shopme.tools.knowledge.taxonomy.DefaultFoodTaxonomyResolver
import de.shopme.tools.knowledge.taxonomy.StringFoodTaxonomyLoader
import org.junit.Test

class FoodSemanticsKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesFoodSemanticsKnowledge() {

        val taxonomyResolver =

            DefaultFoodTaxonomyResolver(

                StringFoodTaxonomyLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/food_taxonomy.json"

                    )

                ).load()

            )

        val semanticsResolver =

            DefaultFoodSemanticsResolver(

                StringFoodSemanticsLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/food_semantics.json"

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

                    TaxonomyCompilerPass(

                        taxonomyResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .add(

                    FoodSemanticsCompilerPass(

                        semanticsResolver

                    )

                )

                .build()

        val writer =

            FoodSemanticsKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/food_semantics.json"

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