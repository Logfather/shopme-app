package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutriScoreCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.NutriScoreKnowledgeWriter
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.nutriscore.DefaultNutriScoreResolver
import de.shopme.tools.knowledge.nutriscore.StringNutriScoreLoader
import org.junit.Test

class NutriScoreKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesNutriScoreKnowledge() {

        val resolver =

            DefaultNutriScoreResolver(

                StringNutriScoreLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/data/v1/nutri_score.json"

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

                    NutriScoreCompilerPass(

                        resolver

                    )

                )

                .build()

        val writer =

            NutriScoreKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/nutri_score.json"

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