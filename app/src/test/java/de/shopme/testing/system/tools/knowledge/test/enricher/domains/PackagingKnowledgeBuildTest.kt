package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.passes.PackagingCompilerPass
import de.shopme.tools.knowledge.compiler.writer.PackagingKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.packaging.DefaultPackagingResolver
import de.shopme.tools.knowledge.packaging.StringPackagingLoader
import org.junit.Test

class PackagingKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesPackagingKnowledge() {

        val packagingResolver =

            DefaultPackagingResolver(

                StringPackagingLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/packaging.json"

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

                    PackagingCompilerPass(

                        packagingResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            PackagingKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/packaging.json"

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