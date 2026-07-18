package de.shopme.tools.knowledge.ai.builder.packaging

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.packaging.Packaging
import de.shopme.tools.knowledge.packaging.PackagingKnowledge

class MergedCandidatePackagingKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): PackagingKnowledge {
        val entries =
            candidates
                .mapNotNull { candidate ->
                    val key =
                        candidate.canonicalId.trim()

                    if (key.isBlank()) {
                        return@mapNotNull null
                    }

                    val payload =
                        candidate.dimensions
                            .firstOrNull { dimension ->
                                dimension.dimension ==
                                        KnowledgeDimensionCandidateType.PACKAGING
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val packaging =
                        payload.toPackaging()
                            ?: return@mapNotNull null

                    key to packaging
                }
                .toMap()
                .toSortedMap()

        return PackagingKnowledge(
            entries = entries
        )
    }

    private fun Any.toPackaging(): Packaging? {
        if (this is Packaging) {
            return this
        }

        if (this !is Map<*, *>) {
            return null
        }

        val values =
            listOf(
                stringValue("packagingText"),
                stringList("packaging").joinToString(" "),
                stringList("materials").joinToString(" "),
                stringList("shapes").joinToString(" ")
            )
                .joinToString(" ")
                .lowercase()

        if (values.isBlank()) {
            return null
        }

        return Packaging(
            score = score(values)
        )
    }

    private fun Map<*, *>.stringValue(
        key: String
    ): String {
        return (this[key] as? String)
            ?.trim()
            ?: ""
    }

    private fun Map<*, *>.stringList(
        key: String
    ): List<String> {
        return (this[key] as? List<*>)
            ?.mapNotNull { it as? String }
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    private fun score(
        value: String
    ): Double {
        return when {
            value.contains("multilayer") ||
                    value.contains("composite") ||
                    value.contains("mixed") ->
                0.9

            value.contains("plastic") ||
                    value.contains("pet") ||
                    value.contains("polyethylene") ||
                    value.contains("polypropylene") ||
                    value.contains("aluminium") ||
                    value.contains("metal") ->
                0.7

            value.contains("glass") ||
                    value.contains("paper") ||
                    value.contains("cardboard") ->
                0.25

            else ->
                0.5
        }
    }
}