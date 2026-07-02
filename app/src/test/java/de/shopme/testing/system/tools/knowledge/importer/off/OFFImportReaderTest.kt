package de.shopme.testing.system.tools.knowledge.importer.off

import de.shopme.tools.knowledge.importer.off.JsonlGzipOFFImportReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OFFImportReaderTest {

    @Test
    fun readsPreviewFileAsStream() {

        val file =
            File("")

        assertTrue(
            "Expected OFF preview file to exist at ${file.path}",
            file.exists()
        )

        val products =
            JsonlGzipOFFImportReader()
                .read(file)

        val firstProducts =
            products
                .take(100)
                .toList()

        assertEquals(
            100,
            firstProducts.size
        )

        assertTrue(
            "Expected at least one product with a name",
            firstProducts.any { product ->
                !product.productName.isNullOrBlank() ||
                        !product.productNameDe.isNullOrBlank()
            }
        )
    }
}