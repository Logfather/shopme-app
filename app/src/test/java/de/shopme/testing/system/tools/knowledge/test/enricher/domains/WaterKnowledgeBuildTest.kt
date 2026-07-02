package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.WaterCompilerPass
import de.shopme.tools.knowledge.compiler.writer.WaterKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.waterfootprint.DefaultWaterResolver
import de.shopme.tools.knowledge.waterfootprint.StringWaterFootprintLoader
import org.junit.Test
import kotlin.test.assertNotNull

class WaterKnowledgeBuildTest : BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesWaterKnowledge() {

        val waterResolver =

            DefaultWaterResolver(

                StringWaterFootprintLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/water_footprint.json"

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

                    WaterCompilerPass(

                        waterResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            WaterKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "data/generated/water_footprint.json"

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

        assertNotNull(

            knowledge.entries["apple"]

        )

        assert(

            knowledge.entries["apple"]!!

                .litersPerKilogram > 0.0

        )

    }

}