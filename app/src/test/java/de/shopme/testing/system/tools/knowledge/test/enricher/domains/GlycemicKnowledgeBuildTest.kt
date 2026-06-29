package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.GlycemicIndexCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.GlycemicKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.glycemic.DefaultGlycemicIndexResolver
import de.shopme.tools.knowledge.glycemic.StringGlycemicLoader
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class GlycemicKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesGlycemicKnowledge() {

        val glycemicResolver =

            DefaultGlycemicIndexResolver(

                StringGlycemicLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/glycemic_index.json"

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

                    GlycemicIndexCompilerPass(

                        glycemicResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            GlycemicKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/glycemic.json"

            )

        assert(output.exists())

        assert(output.length() > 0)
    }

}