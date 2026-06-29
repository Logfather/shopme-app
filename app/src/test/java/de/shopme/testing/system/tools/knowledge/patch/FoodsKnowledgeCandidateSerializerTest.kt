package de.shopme.testing.system.tools.knowledge.artifacts

import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeCandidateSerializer
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodsKnowledgeCandidateSerializerTest {

    private val serializer =
        FoodsKnowledgeCandidateSerializer()

    @Test
    fun serializeWritesCandidateIdsSortedByCanonicalId() {

        val json =
            serializer.serialize(
                candidates = listOf(
                    candidate("pear"),
                    candidate("apple"),
                    candidate("banana")
                )
            )

        assertEquals(
            """
            [
              {
                "id": "apple"
              },
              {
                "id": "banana"
              },
              {
                "id": "pear"
              }
            ]
            """.trimIndent(),
            json
        )
    }

    @Test
    fun serializeEmptyListWritesEmptyJsonArray() {

        val json =
            serializer.serialize(
                candidates = emptyList()
            )

        assertEquals(
            """
            [
            
            ]
            """.trimIndent(),
            json
        )
    }

    @Test
    fun serializeContainsCanonicalId() {

        val json =
            serializer.serialize(
                candidates = listOf(
                    candidate("apple")
                )
            )

        assertTrue(
            json.contains("\"id\": \"apple\"")
        )
    }

    private fun candidate(
        canonicalId: String
    ): CanonicalKnowledgeCandidate {

        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions = emptyList(),
            metadata = CandidateMetadata(
                confidence = 1.0,
                source = "test",
                sourceId = canonicalId,
                version = "1"
            )
        )
    }
}