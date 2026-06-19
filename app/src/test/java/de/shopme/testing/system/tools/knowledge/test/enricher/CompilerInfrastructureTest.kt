package de.shopme.testing.system.tools.knowledge.test.enricher

import de.shopme.tools.knowledge.compiler.DefaultFoodKnowledgeBuildCompiler
import de.shopme.tools.knowledge.reader.ResourceCatalogReader
import org.junit.Test
import kotlin.test.assertNotNull

class CompilerInfrastructureTest {

    @Test
    fun buildCompilerExists() {

        val compiler =

            DefaultFoodKnowledgeBuildCompiler.create()

        assertNotNull(

            compiler

        )


    }

    @Test
    fun buildCompilerCanBeCreatedWithDefaultPipeline() {

        val compiler =

            DefaultFoodKnowledgeBuildCompiler.create()

        assertNotNull(

            compiler

        )

    }

    @Test
    fun buildCompilerCanCompileCatalog() {

        val compiler =

            DefaultFoodKnowledgeBuildCompiler.create()

        val reader =

            ResourceCatalogReader()

        val entries =

            reader.read()

        assert(entries.isNotEmpty())

        val result =

            compiler.compile(

                entries.first()

            )

        assertNotNull(

            result

        )

    }
}