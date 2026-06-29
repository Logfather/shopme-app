package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.carbon.DefaultCarbonFootprintResolver
import de.shopme.tools.knowledge.carbon.StringCarbonFootprintLoader
import de.shopme.tools.knowledge.compiler.passes.CarbonCompilerPass
import de.shopme.tools.knowledge.compiler.passes.CarbonImpactCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.CarbonImpactKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class CarbonImpactKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesCarbonImpactKnowledge() {

        val carbonResolver =

            DefaultCarbonFootprintResolver(

                StringCarbonFootprintLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/carbon_footprint.json"

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

                    CarbonCompilerPass(
                        carbonResolver,
                        foodLookup = EmptyFoodLookup
                    )

                )

                .add(

                    CarbonImpactCompilerPass(
                        foodLookup = EmptyFoodLookup)

                )

                .build()

        val writer =

            CarbonImpactKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/carbon_impact.json"

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