package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.patch.DefaultFoodsPatchWriter
import de.shopme.tools.knowledge.patch.DefaultFoodsPatchWriterFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFoodsPatchWriterFactoryTest {

    @Test
    fun createReturnsDefaultFoodsPatchWriter() {

        val writer =
            DefaultFoodsPatchWriterFactory.create()

        assertTrue(
            writer is DefaultFoodsPatchWriter
        )
    }

}