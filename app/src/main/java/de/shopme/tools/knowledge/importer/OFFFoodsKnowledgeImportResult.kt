package de.shopme.tools.knowledge.importer

import de.shopme.tools.knowledge.foods.FoodsKnowledge

data class OFFFoodsKnowledgeImportResult(

    val knowledge: FoodsKnowledge,

    val scanned: Int,

    val imported: Int,

    val unique: Int,

    val nameCounts: Map<String, Int>

)