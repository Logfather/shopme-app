package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompiler
import de.shopme.tools.knowledge.compiler.writer.FoodKnowledgeWriter
import de.shopme.tools.knowledge.loader.ResourceKnowledgeLoader
import de.shopme.tools.knowledge.nutrition.DefaultNutritionAliasResolver
import de.shopme.tools.knowledge.nutrition.StringNutritionAliasLoader
import de.shopme.tools.knowledge.reader.ResourceCatalogReader

abstract class BaseKnowledgeBuildTest {

    protected val aliasResolver =

        DefaultNutritionAliasResolver(

            StringNutritionAliasLoader(

                ResourceKnowledgeLoader.load(

                    "knowledge/data/v1/nutrition_alias.json"

                )

            ).load()

        )

    protected fun runBuild(

        compiler: FoodKnowledgeCompiler,

        writer: FoodKnowledgeWriter

    ) {

        writer.begin()

        ResourceCatalogReader()

            .read()

            .forEach { item ->

                writer.write(

                    compiler.compileContext(

                        item

                    )

                )

            }

        writer.finish()

    }

}