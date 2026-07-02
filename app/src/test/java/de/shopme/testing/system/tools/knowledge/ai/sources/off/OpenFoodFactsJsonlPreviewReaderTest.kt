package de.shopme.testing.system.tools.knowledge.ai.sources.off

import com.google.gson.Gson
import de.shopme.tools.knowledge.ai.sources.off.OFFJsonlPreviewReader
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.zip.GZIPOutputStream

class OpenFoodFactsJsonlPreviewReaderTest {

    @Test
    fun readReadsLimitedProductsFromGzippedJsonlFile() {

        val file =
            File.createTempFile(
                "off-preview",
                ".jsonl.gz"
            )

        try {
            writeGzipJsonl(
                file = file,
                lines = listOf(
                    """{"code":"123","product_name":"Banana","categories":"Fruits","ingredients_text":"Banana","nutrition_grade_fr":"a","nova_group":1,"nutriments":{"energy-kcal_100g":89.0,"fat_100g":0.3,"sugars_100g":12.2,"proteins_100g":1.1,"salt_100g":0.0}}""",
                    """{"code":"456","product_name":"Apple","categories":"Fruits"}"""
                )
            )

            val products =
                OFFJsonlPreviewReader(
                    gson = Gson()
                ).read(
                    file = file,
                    limit = 1
                )

            assertEquals(
                1,
                products.size
            )

            val product =
                products.single()

            assertEquals(
                "123",
                product.code
            )

            assertEquals(
                "Banana",
                product.productName
            )

            assertEquals(
                "Fruits",
                product.categories
            )

            assertEquals(
                89.0,
                product.energyKcal100g
            )

            assertEquals(
                12.2,
                product.sugars100g
            )

        } finally {
            file.delete()
        }
    }

    private fun writeGzipJsonl(
        file: File,
        lines: List<String>
    ) {

        GZIPOutputStream(
            file.outputStream()
        ).bufferedWriter()
            .use { writer ->
                lines.forEach { line ->
                    writer.write(line)
                    writer.newLine()
                }
            }
    }
}