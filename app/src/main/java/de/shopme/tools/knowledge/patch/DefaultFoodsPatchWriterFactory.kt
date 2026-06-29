package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeCandidateSerializer

object DefaultFoodsPatchWriterFactory {

    fun create(): FoodsPatchWriter {

        return DefaultFoodsPatchWriter(

            serializer =
                FoodsKnowledgeCandidateSerializer()

        )
    }
}