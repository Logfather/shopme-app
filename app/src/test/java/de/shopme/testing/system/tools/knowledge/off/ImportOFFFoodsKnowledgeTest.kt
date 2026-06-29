package de.shopme.testing.system.tools.knowledge.off

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
                    input = File(
                        "build/input/off-products.jsonl.gz"
                    )
                )

        FoodsKnowledgeWriter()
            .write(
                knowledge = foodsKnowledge,
                outputFile = File(
                    "build/generated/foods_off.json"
                )
            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OPEN FOOD FACTS CANONICAL IMPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Foods : ${foodsKnowledge.foods.size}")
        println("Output: build/generated/foods_off.json")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}