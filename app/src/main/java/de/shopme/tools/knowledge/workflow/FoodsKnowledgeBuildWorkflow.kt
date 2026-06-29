package de.shopme.tools.knowledge.workflow

import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeArtifactWriter
import de.shopme.tools.knowledge.pipeline.FoodsKnowledgeArtifactGenerator
import de.shopme.tools.knowledge.pipeline.KnowledgeCandidateBuildPipeline

class FoodsKnowledgeBuildWorkflow(
    private val pipeline: KnowledgeCandidateBuildPipeline,
    private val generator: FoodsKnowledgeArtifactGenerator,
    private val writer: FoodsKnowledgeArtifactWriter
) {

    fun build() {

        val result =
            pipeline.build()

        val artifact =
            generator.generate(
                result.validCandidates
            )

        writer.write(
            artifact
        )

    }

}