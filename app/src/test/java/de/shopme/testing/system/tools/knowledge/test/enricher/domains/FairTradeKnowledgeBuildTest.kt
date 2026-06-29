package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.FairTradeCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.FairTradeKnowledgeWriter
import de.shopme.tools.knowledge.fairtrade.DefaultFairTradeResolver
import de.shopme.tools.knowledge.fairtrade.StringFairTradeLoader
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class FairTradeKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesFairTradeKnowledge() {

        val fairTradeResolver =

            DefaultFairTradeResolver(

                StringFairTradeLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/fair_trade.json"

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

                    FairTradeCompilerPass(

                        fairTradeResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            FairTradeKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/fairtrade.json"

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

                "apple"

            )

        )

        assert(

            knowledge.entries["apple"]!!

                .score >= 0.0

        )

    }

}