package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.biodiversity.DefaultBiodiversityResolver
import de.shopme.tools.knowledge.biodiversity.StringBiodiversityLoader
import de.shopme.tools.knowledge.compiler.passes.BiodiversityCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.BiodiversityKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class BiodiversityKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesBiodiversityKnowledge() {

        val biodiversityResolver =

            DefaultBiodiversityResolver(

                StringBiodiversityLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/biodiversity.json"

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

                    BiodiversityCompilerPass(

                        biodiversityResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            BiodiversityKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "data/generated/biodiversity.json"

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