package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.data.KnowledgeDataDirectories
import de.shopme.tools.knowledge.foods.FoodsKnowledgeWriter
import de.shopme.tools.knowledge.foods.importer.OFFFoodsKnowledgeImporter
import java.io.File
import kotlin.test.Test

class ImportOFFFoodsKnowledgeTest {

    @Test
    fun importOpenFoodFactsIntoCanonicalFoods() {

        val foodsKnowledge =
            OFFFoodsKnowledgeImporter()
                .import(
                    File(
                        KnowledgeDataDirectories.openFoodFactsRaw,
                        "off-products.jsonl.gz"
                    )
                )

        FoodsKnowledgeWriter()
            .write(
                knowledge = foodsKnowledge,
                outputFile = File(
                    "data/generated/foods_off.json"
                )
            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OPEN FOOD FACTS CANONICAL IMPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Foods : ${foodsKnowledge.foods.size}")
        println("Output: data/generated/foods_off.json")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}