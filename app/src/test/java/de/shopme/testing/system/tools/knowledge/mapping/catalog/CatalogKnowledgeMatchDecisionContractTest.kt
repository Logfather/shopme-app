package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogKnowledgeMatchDecisionContractTest {

    @Test
    fun createsMatchDecision() {

        val decision =
            CatalogKnowledgeMatchDecision(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                type =
                    CatalogKnowledgeMatchDecisionType.MATCH,
                selectedServerKey =
                    "milk semi skimmed uht",
                confidence =
                    0.97,
                reason =
                    "The catalog key describes semi-skimmed UHT milk"
            )

        assertTrue(
            decision.isMatch
        )

        assertEquals(
            "milk semi skimmed uht",
            decision.selectedServerKey
        )
    }


    @Test
    fun createsNoMatchDecision() {

        val decision =
            CatalogKnowledgeMatchDecision(
                catalogKey =
                    "salami pizza",
                serverArtifact =
                    "environmental_impact.json",
                type =
                    CatalogKnowledgeMatchDecisionType.NO_MATCH,
                selectedServerKey =
                    null,
                confidence =
                    0.88,
                reason =
                    "None of the candidates represents salami pizza"
            )

        assertFalse(
            decision.isMatch
        )

        assertEquals(
            null,
            decision.selectedServerKey
        )
    }


    @Test
    fun rejectsMatchWithoutSelectedServerKey() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchDecision(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                type =
                    CatalogKnowledgeMatchDecisionType.MATCH,
                selectedServerKey =
                    null,
                confidence =
                    0.97,
                reason =
                    "Semantic match"
            )
        }
    }


    @Test
    fun rejectsNoMatchWithSelectedServerKey() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchDecision(
                catalogKey =
                    "salami pizza",
                serverArtifact =
                    "environmental_impact.json",
                type =
                    CatalogKnowledgeMatchDecisionType.NO_MATCH,
                selectedServerKey =
                    "pizza ham cheese frozen",
                confidence =
                    0.88,
                reason =
                    "No acceptable match"
            )
        }
    }


    @Test
    fun rejectsBlankReason() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchDecision(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                type =
                    CatalogKnowledgeMatchDecisionType.MATCH,
                selectedServerKey =
                    "milk semi skimmed uht",
                confidence =
                    0.97,
                reason =
                    " "
            )
        }
    }


    @Test
    fun rejectsInvalidConfidence() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchDecision(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                type =
                    CatalogKnowledgeMatchDecisionType.MATCH,
                selectedServerKey =
                    "milk semi skimmed uht",
                confidence =
                    1.01,
                reason =
                    "Semantic match"
            )
        }
    }


    @Test
    fun createsVersionedDecisionBatch() {

        val decisions =
            listOf(
                CatalogKnowledgeMatchDecision(
                    catalogKey =
                        "semi skimmed milk",
                    serverArtifact =
                        "environmental_impact.json",
                    type =
                        CatalogKnowledgeMatchDecisionType.MATCH,
                    selectedServerKey =
                        "milk semi skimmed uht",
                    confidence =
                        0.97,
                    reason =
                        "Semantic match"
                ),
                CatalogKnowledgeMatchDecision(
                    catalogKey =
                        "salami pizza",
                    serverArtifact =
                        "environmental_impact.json",
                    type =
                        CatalogKnowledgeMatchDecisionType.NO_MATCH,
                    selectedServerKey =
                        null,
                    confidence =
                        0.88,
                    reason =
                        "No acceptable candidate"
                )
            ).sortedWith(
                CatalogKnowledgeMatchDecisions.DECISION_ORDER
            )

        val batch =
            CatalogKnowledgeMatchDecisions(
                version =
                    CatalogKnowledgeMatchDecisionContract.CURRENT_VERSION,
                decisions =
                    decisions
            )

        assertEquals(
            1,
            batch.version
        )

        assertEquals(
            2,
            batch.decisions.size
        )
    }


    @Test
    fun rejectsDuplicateDecisionForCatalogKeyAndArtifact() {

        val decision =
            CatalogKnowledgeMatchDecision(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                type =
                    CatalogKnowledgeMatchDecisionType.MATCH,
                selectedServerKey =
                    "milk semi skimmed uht",
                confidence =
                    0.97,
                reason =
                    "Semantic match"
            )

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchDecisions(
                version =
                    CatalogKnowledgeMatchDecisionContract.CURRENT_VERSION,
                decisions =
                    listOf(
                        decision,
                        decision
                    )
            )
        }
    }


    @Test
    fun rejectsNonDeterministicDecisionOrder() {

        val decisions =
            listOf(
                CatalogKnowledgeMatchDecision(
                    catalogKey =
                        "semi skimmed milk",
                    serverArtifact =
                        "nutrition.json",
                    type =
                        CatalogKnowledgeMatchDecisionType.MATCH,
                    selectedServerKey =
                        "milk semi skimmed",
                    confidence =
                        0.96,
                    reason =
                        "Semantic match"
                ),
                CatalogKnowledgeMatchDecision(
                    catalogKey =
                        "apple juice",
                    serverArtifact =
                        "environmental_impact.json",
                    type =
                        CatalogKnowledgeMatchDecisionType.MATCH,
                    selectedServerKey =
                        "apple juice pure juice",
                    confidence =
                        0.94,
                    reason =
                        "Semantic match"
                )
            )

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchDecisions(
                version =
                    CatalogKnowledgeMatchDecisionContract.CURRENT_VERSION,
                decisions =
                    decisions
            )
        }
    }
}