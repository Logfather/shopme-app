package de.shopme.tools.knowledge.compiler.catalog

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeCandidateProcessor
import de.shopme.tools.knowledge.compiler.candidate.CanonicalKnowledgeCandidateToPatchOperationMapper
import de.shopme.tools.knowledge.compiler.candidate.DefaultFoodsJsonPatchApplier
import java.io.File

object DefaultFileCatalogUpdateWorkflowFactory {

    fun create(
        file: File
    ): FileCatalogUpdateWorkflow {

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        val reader = FileCatalogReader(
            deserializer = GsonCatalogJsonDeserializer(gson),
            inputFile = file
        )

        val writer = JsonCatalogWriter(
            serializer = GsonCatalogJsonSerializer(gson),
            outputFile = file
        )

        val updateWorkflow = DefaultAIKnowledgeCatalogUpdateWorkflow(
            patchApplier = DefaultFoodsJsonPatchApplier(),
            catalogWriter = writer
        )

        val importWorkflow = DefaultAIKnowledgeCatalogImportWorkflow(
            candidateProcessor = DefaultAIKnowledgeCandidateProcessor(),
            patchOperationMapper = CanonicalKnowledgeCandidateToPatchOperationMapper(),
            catalogUpdateWorkflow = updateWorkflow
        )

        return FileCatalogUpdateWorkflow(
            reader = reader,
            updateWorkflow = updateWorkflow,
            importWorkflow = importWorkflow
        )
    }
}