package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.carbon.DefaultCarbonFootprintResolver
import de.shopme.tools.knowledge.carbon.StringCarbonFootprintLoader
import de.shopme.tools.knowledge.compiler.passes.CarbonCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.CarbonKnowledgeWriter
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class CarbonKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesCarbonKnowledge() {

        val carbonResolver =

            DefaultCarbonFootprintResolver(

                StringCarbonFootprintLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/data/v1/carbon_footprint.json"

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

                    CarbonCompilerPass(

                        carbonResolver

                    )

                )

                .build()

        val writer =

            CarbonKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/carbon_footprint.json"

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

                .kilogramsPerKilogram >= 0.0

        )

    }

}