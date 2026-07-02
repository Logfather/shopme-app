package de.shopme.testing.system.tools.knowledge.test.publisher

import de.shopme.tools.knowledge.publisher.RuntimeKnowledgePublisher
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class RuntimeKnowledgePublishBuildTest {

    @Test
    fun publishGeneratedRuntimeKnowledge() {

        val generatedDirectory =
            File("data/generated")

        val runtimeDirectory =
            File("data/generated/runtimeKnowledge")

        assertTrue(
            generatedDirectory.exists(),
            "data/generated does not exist. Run the knowledge build tests first."
        )

        RuntimeKnowledgePublisher(
            generatedDirectory = generatedDirectory,
            runtimeDirectory = runtimeDirectory
        ).publish()

        assertTrue(
            runtimeDirectory.exists()
        )

        assertTrue(
            runtimeDirectory
                .listFiles()
                ?.any { it.isFile && it.extension == "json" } == true
        )
    }
}