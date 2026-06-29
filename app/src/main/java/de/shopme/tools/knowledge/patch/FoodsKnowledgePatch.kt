package de.shopme.tools.knowledge.patch

data class FoodsKnowledgePatch(

    val metadata: FoodsKnowledgePatchMetadata,

    val entries: List<FoodsKnowledgePatchEntry>

)