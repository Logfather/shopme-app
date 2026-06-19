package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.SeasonalityCompilerPass
import de.shopme.tools.knowledge.compiler.writer.SeasonalityKnowledgeWriter
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.seasonality.DefaultSeasonalityResolver
import de.shopme.tools.knowledge.seasonality.StringSeasonalityLoader
import org.junit.Test

class SeasonalityKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesSeasonalityKnowledge() {

        val resolver =

            DefaultSeasonalityResolver(

                StringSeasonalityLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/data/v1/seasonality.json"

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

                    SeasonalityCompilerPass(

                        resolver

                    )

                )

                .build()

        val writer =

            SeasonalityKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/seasonality.json"

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