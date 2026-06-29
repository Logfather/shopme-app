package de.shopme.tools.knowledge.patch

import java.time.Instant

data class FoodsKnowledgePatchMetadata(

    val source: String,

    val generatedAt: Instant,

    val version: String

)