package de.shopme.tools.knowledge.ki_candidates.normalizer

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class KnowledgeCandidateNormalizer {

    fun normalize(
        candidate: CanonicalKnowledgeCandidate
    ): CanonicalKnowledgeCandidate {

        val canonical =
            normalizeText(candidate.canonicalId)

        val aliases =
            normalizeAliases(
                candidate.aliases + candidate.canonicalId
            )

        return candidate.copy(
            canonicalId = canonical,
            aliases = normalizeAliases(
                candidate.aliases + candidate.canonicalId
            ),
            matchAliases = normalizeAliases(candidate.matchAliases)
        )
    }

    fun normalize(
        candidates: List<CanonicalKnowledgeCandidate>
    ): List<CanonicalKnowledgeCandidate> {
        return candidates.map(::normalize)
    }

    private fun normalizeAliases(
        aliases: Set<String>
    ): Set<String> {

        if (aliases.isEmpty()) {
            return emptySet()
        }

        val normalized =
            linkedSetOf<String>()

        for (alias in aliases) {
            if (normalized.size >= 20) {
                break
            }

            val value =
                normalizeText(alias)

            if (value.isNotBlank()) {
                normalized += value
            }
        }

        return normalized
    }

    private fun normalizeText(
        value: String
    ): String {

        return value
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .collapseWhitespace()
            .trim()
    }

    private fun String.collapseWhitespace(): String {

        if (isEmpty()) {
            return this
        }

        val builder =
            StringBuilder(length)

        var previousWasWhitespace =
            false

        for (char in this) {
            if (char.isWhitespace()) {
                if (!previousWasWhitespace) {
                    builder.append(' ')
                    previousWasWhitespace = true
                }
            } else {
                builder.append(char)
                previousWasWhitespace = false
            }
        }

        return builder.toString()
    }

    private fun removeTrailingDescriptor(
        value: String
    ): String {
        val descriptorMarkers =
            listOf(
                ", powder",
                ", raw",
                ", cooked",
                ", dried",
                ", dehydrated",
                ", with grains",
                ", no precision"
            )

        return descriptorMarkers
            .firstOrNull { marker ->
                value.endsWith(marker)
            }
            ?.let { marker ->
                value.removeSuffix(marker).trim()
            }
            ?: value
    }

    private fun reorderCommaDescriptor(
        value: String
    ): String {
        val parts =
            value.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

        if (parts.size != 2) {
            return value
        }

        return "${parts[1]} ${parts[0]}".trim()
    }
}