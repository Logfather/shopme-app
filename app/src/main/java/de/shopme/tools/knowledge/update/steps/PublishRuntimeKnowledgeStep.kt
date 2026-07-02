package de.shopme.tools.knowledge.update.steps

import java.io.File

class PublishRuntimeKnowledgeStep :

    KnowledgeUpdateStep,
    KnowledgePublishStep {

    val generatedCarbonKnowledge =

        File(
            "data/generated/carbon_footprint.json"
        )

    val runtimeCarbonKnowledge =

        File(
            "src/main/assets/knowledge/runtime/carbon_footprint.json"
        )

    override fun publish() {

        require(
            generatedCarbonKnowledge.exists()
        ) {
            "Generated carbon knowledge not found: ${generatedCarbonKnowledge.path}"
        }

        runtimeCarbonKnowledge
            .parentFile
            ?.mkdirs()

        generatedCarbonKnowledge.copyTo(
            target = runtimeCarbonKnowledge,
            overwrite = true
        )
    }

    override fun execute() {

        publish()

    }
}