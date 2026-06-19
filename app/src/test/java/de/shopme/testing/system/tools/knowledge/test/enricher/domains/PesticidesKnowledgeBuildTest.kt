package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.PesticideCompilerPass
import de.shopme.tools.knowledge.compiler.writer.PesticidesKnowledgeWriter
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.pesticide.DefaultPesticideResolver
import de.shopme.tools.knowledge.pesticide.StringPesticideLoader
import org.junit.Test

class PesticidesKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesPesticidesKnowledge() {

        val pesticidesResolver =

            DefaultPesticideResolver(

                StringPesticideLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/data/v1/pesticide.json"

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

                    PesticideCompilerPass(

                        pesticidesResolver

                    )

                )

                .build()

        val writer =

            PesticidesKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/pesticides.json"

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