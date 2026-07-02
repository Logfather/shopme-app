package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.ProductionCompilerPass
import de.shopme.tools.knowledge.compiler.writer.ProductionKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.production.DefaultProductionResolver
import de.shopme.tools.knowledge.production.StringProductionLoader
import org.junit.Test

class ProductionKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesProductionKnowledge() {

        val resolver =

            DefaultProductionResolver(

                StringProductionLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/production.json"

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

                    ProductionCompilerPass(

                        resolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            ProductionKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "data/generated/production.json"

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