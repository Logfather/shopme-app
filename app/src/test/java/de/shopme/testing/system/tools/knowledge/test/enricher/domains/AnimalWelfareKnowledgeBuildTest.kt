package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.testing.system.tools.knowledge.test.enricher.TestFoodKnowledgeCompilerBuilder
import de.shopme.tools.knowledge.animalwelfare.DefaultAnimalWelfareResolver
import de.shopme.tools.knowledge.animalwelfare.StringAnimalWelfareLoader
import de.shopme.tools.knowledge.compiler.passes.AnimalWelfareCompilerPass
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.compiler.writer.AnimalWelfareKnowledgeWriter
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import org.junit.Test

class AnimalWelfareKnowledgeBuildTest :

    BaseKnowledgeBuildTest() {

    @Test
    fun buildCompilerCreatesAnimalWelfareKnowledge() {

        val animalWelfareResolver =

            DefaultAnimalWelfareResolver(

                StringAnimalWelfareLoader(

                    ResourceKnowledgeLoader.load(

                        "knowledge/runtime/animal_welfare.json"

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

                    AnimalWelfareCompilerPass(

                        animalWelfareResolver,
                        foodLookup = EmptyFoodLookup

                    )

                )

                .build()

        val writer =

            AnimalWelfareKnowledgeWriter()

        runBuild(

            compiler,

            writer

        )

        val output =

            java.io.File(

                "build/generated/animal_welfare.json"

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

                "beef"

            )

        )

        assert(

            knowledge.entries["beef"]!!

                .score == 0.20

        )

    }

}