package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeCandidateSerializer
import java.io.File

class DefaultFoodsPatchWriter(

    private val serializer:
    FoodsKnowledgeCandidateSerializer

) : FoodsPatchWriter {

    override fun write(
        result: FoodsPatchApplyResult,
        outputFile: String
    ): FoodsPatchWriteResult {

        val json =
            serializer.serialize(
                result.candidates
            )

        File(outputFile)
            .apply {
                parentFile?.mkdirs()
            }
            .writeText(json)

        return FoodsPatchWriteResult(
            candidates = result.candidates,
            applyResult = result,
            serializedJson = json,
            stats = FoodsPatchWriteStats(
                candidateCount = result.candidates.size,
                outputFile = outputFile
            )
        )
    }
}