package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.TaxonomyCompilerPass
import de.shopme.tools.knowledge.compiler.writer.FoodTaxonomyKnowledgeWriter
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.taxonomy.DefaultFoodTaxonomyResolver
import de.shopme.tools.knowledge.taxonomy.StringFoodTaxonomyLoader
import org.junit.Test

class FoodTaxonomyKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesFoodTaxonomyKnowledge() {

        val resolver =

            DefaultFoodTaxonomyResolver(

                StringFoodTaxonomyLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/data/v1/food_taxonomy.json"

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

                    TaxonomyCompilerPass(

                        resolver

                    )

                )

                .build()

        val writer =

            FoodTaxonomyKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/food_taxonomy.json"

            )

        assert(

            output.exists()

        )

        assert(

            output.length() > 0

        )

        assert(

            writer.knowledge()

                .entries.isNotEmpty()

        )

    }

}