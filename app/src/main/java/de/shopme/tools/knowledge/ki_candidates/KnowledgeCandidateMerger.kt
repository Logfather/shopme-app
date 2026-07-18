package de.shopme.tools.knowledge.ki_candidates

class KnowledgeCandidateMerger(
    private val sourcePriority: KnowledgeCandidateSourcePriority =
        KnowledgeCandidateSourcePriority(),
    private val compatibilityPolicy: CandidateCompatibilityPolicy =
        CandidateCompatibilityPolicy()
) {

    val blockedHighFanoutKeys =
        mutableMapOf<String, Int>()

    fun merge(
        candidates: List<CanonicalKnowledgeCandidate>
    ): KnowledgeCandidateMergeResult {

        val conflicts =
            mutableListOf<KnowledgeCandidateMergeConflict>()

        val mergedCandidates =
            groupByCanonicalIdOrAlias(candidates)
                .map { group ->
                    mergeGroup(
                        candidates = group,
                        conflicts = conflicts
                    )
                }

        return KnowledgeCandidateMergeResult(
            candidates = mergedCandidates,
            conflicts = conflicts
        )
    }

    private fun groupByCanonicalIdOrAlias(
        candidates: List<CanonicalKnowledgeCandidate>
    ): List<List<CanonicalKnowledgeCandidate>> {

        val indexByKey =
            mutableMapOf<String, MutableList<Int>>()

        candidates.forEachIndexed { index, candidate ->
            candidate.matchKeys().forEach { key ->
                indexByKey
                    .getOrPut(key) { mutableListOf() }
                    .add(index)
            }
        }

        val safeIndexByKey =
            indexByKey.filterValues { indexes ->
                indexes.size <= MAX_MATCH_KEY_FANOUT
            }

        val visited =
            mutableSetOf<Int>()

        val groups =
            mutableListOf<List<CanonicalKnowledgeCandidate>>()

        candidates.forEachIndexed { index, candidate ->

            if (index in visited) {
                return@forEachIndexed
            }

            val directMatches =
                candidate
                    .matchKeys()
                    .flatMap { key ->
                        safeIndexByKey[key].orEmpty()
                    }
                    .toSortedSet()
                    .filter { matchedIndex ->
                        matchedIndex == index ||
                                compatibilityPolicy.areCompatible(
                                    base = candidate,
                                    candidate = candidates[matchedIndex]
                                )
                    }
                    .toMutableSet()
                    .also { matches ->
                        matches += index
                    }
                    .toSortedSet()

            visited += directMatches

            groups += directMatches.map { matchedIndex ->
                candidates[matchedIndex]
            }
        }

        return groups
    }

    private fun mergeGroup(
        candidates: List<CanonicalKnowledgeCandidate>,
        conflicts: MutableList<KnowledgeCandidateMergeConflict>
    ): CanonicalKnowledgeCandidate {

        val primary =
            candidates.first()

        val dimensionsByType =
            candidates
                .flatMap { candidate ->
                    candidate.dimensions.map { dimension ->
                        candidate to dimension
                    }
                }
                .groupBy { entry ->
                    entry.second.dimension
                }

        val mergedDimensions =
            dimensionsByType.map { (type, entries) ->

                val distinctPayloads =
                    entries
                        .map { entry ->
                            entry.second.payload
                        }
                        .distinct()

                val selectedDimension =
                    selectWinner(entries)

                if (distinctPayloads.size > 1) {
                    conflicts += createConflict(
                        canonicalId = primary.canonicalId,
                        type = type,
                        entries = entries,
                        selectedDimension = selectedDimension,
                        distinctPayloads = distinctPayloads
                    )
                }

                selectedDimension
            }

        return CanonicalKnowledgeCandidate(
            canonicalId = primary.canonicalId,
            aliases =
                candidates
                    .flatMap { candidate ->
                        buildSet {
                            add(candidate.canonicalId)

                            candidate.aliases
                                .filter { alias ->
                                    alias.isOutputAlias()
                                }
                                .forEach(::add)
                        }
                    }
                    .filter { it.isNotBlank() }
                    .toSortedSet(),
            matchAliases = emptySet(),
            dimensions = mergedDimensions,
            metadata = primary.metadata
        )
    }

    private fun createConflict(
        canonicalId: String,
        type: KnowledgeDimensionCandidateType,
        entries: List<Pair<CanonicalKnowledgeCandidate, KnowledgeDimensionCandidate>>,
        selectedDimension: KnowledgeDimensionCandidate,
        distinctPayloads: List<Any>
    ): KnowledgeCandidateMergeConflict {

        val rejectedPayloads =
            distinctPayloads
                .filterNot { payload ->
                    payload == selectedDimension.payload
                }

        val resolution =
            if (type == KnowledgeDimensionCandidateType.NUTRITION) {
                createNutritionResolution(
                    entries = entries,
                    selectedPayload = selectedDimension.payload
                )
            } else {
                null
            }

        return KnowledgeCandidateMergeConflict(
            canonicalId = canonicalId,
            dimension = type,
            selectedPayload = selectedDimension.payload,
            rejectedPayloads = rejectedPayloads,
            resolution = resolution
        )
    }

    private fun createNutritionResolution(
        entries: List<Pair<CanonicalKnowledgeCandidate, KnowledgeDimensionCandidate>>,
        selectedPayload: Any
    ): KnowledgeConflictResolutionMetadata {

        val selectedScore =
            scoreNutritionPayload(selectedPayload)

        val rejectedScores =
            entries
                .map { entry ->
                    entry.second.payload
                }
                .distinct()
                .filterNot { payload ->
                    payload == selectedPayload
                }
                .map { payload ->
                    scoreNutritionPayload(payload)
                }

        return KnowledgeConflictResolutionMetadata(
            type = KnowledgeConflictResolutionType.QUALITY_SCORE,
            alternatives = entries
                .map { entry ->
                    entry.second.payload
                }
                .distinct()
                .size,
            selectedScore = selectedScore,
            rejectedScores = rejectedScores,
            confidence = confidenceFor(
                selectedScore = selectedScore,
                rejectedScores = rejectedScores
            )
        )
    }

    private fun confidenceFor(
        selectedScore: Int,
        rejectedScores: List<Int>
    ): KnowledgeConflictResolutionConfidence {

        val bestRejected =
            rejectedScores.maxOrNull()
                ?: return KnowledgeConflictResolutionConfidence.HIGH

        val delta =
            selectedScore - bestRejected

        return when {
            delta >= 20 -> KnowledgeConflictResolutionConfidence.HIGH
            delta >= 5 -> KnowledgeConflictResolutionConfidence.MEDIUM
            else -> KnowledgeConflictResolutionConfidence.LOW
        }
    }

    private fun selectWinner(
        entries: List<Pair<CanonicalKnowledgeCandidate, KnowledgeDimensionCandidate>>
    ): KnowledgeDimensionCandidate {

        return entries
            .maxWith(
                compareBy<Pair<CanonicalKnowledgeCandidate, KnowledgeDimensionCandidate>> { entry ->
                    sourcePriority.priority(
                        dimension = entry.second.dimension,
                        source = entry.first.metadata.source
                    )
                }.thenBy { entry ->
                    if (entry.second.dimension == KnowledgeDimensionCandidateType.NUTRITION) {
                        scoreNutritionPayload(entry.second.payload)
                    } else {
                        0
                    }
                }
            )
            .second
    }

    private fun scoreNutritionPayload(
        payload: Any
    ): Int {

        val nutrition =
            payload as? Map<*, *>
                ?: return 0

        val values =
            nutrition.values
                .mapNotNull { value ->
                    value as? Number
                }
                .map { value ->
                    value.toDouble()
                }

        val presentFields =
            values.size

        val nonZeroFields =
            values.count { value ->
                value > 0.0
            }

        val hasEnergy =
            nutrition["energyKcalPer100g"] is Number

        val hasMacros =
            nutrition["fatPer100g"] is Number &&
                    nutrition["carbohydratesPer100g"] is Number &&
                    nutrition["proteinsPer100g"] is Number

        return presentFields * 10 +
                nonZeroFields * 3 +
                if (hasEnergy) 15 else 0 +
                        if (hasMacros) 20 else 0
    }

    private fun CanonicalKnowledgeCandidate.matchKeys(): Set<String> {
        return buildSet {
            add(canonicalId.normalizedMatchKey())

            aliases
                .filter { alias ->
                    alias.isSafeIdentityAlias()
                }
                .forEach { alias ->
                    add(alias.normalizedMatchKey())
                }

            matchAliases
                .filter { alias ->
                    alias.normalizedMatchKey().isSafeMatchKey()
                }
                .forEach { alias ->
                    add(alias.normalizedMatchKey())
                }
        }
            .filter { key ->
                key.isSafeMatchKey()
            }
            .toSet()
    }

    private fun String.isSafeMatchKey(): Boolean {

        if (isBlank()) {
            return false
        }

        if (length < 3) {
            return false
        }

        return this !in unsafeGenericMatchKeys
    }

    private val unsafeGenericMatchKeys =
        setOf(
            "raw",
            "cooked",
            "dried",
            "dehydrated",
            "powder",
            "fresh",
            "frozen",
            "oil",
            "sauce",
            "vinegar",
            "mustard",
            "cream",
            "milk",
            "cheese",
            "yogurt",
            "rice",
            "pasta",
            "bread",
            "juice",
            "drink",
            "water",
            "meat",
            "fish",
            "fruit",
            "vegetable",
            "salt",
            "sugar"
        )

    private fun String.normalizedMatchKey(): String {
        return trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }

    private fun String.isSafeIdentityAlias(): Boolean {

        val normalized =
            normalizedMatchKey()

        if (!normalized.isSafeMatchKey()) {
            return false
        }

        if (normalized in unsafeIdentityAliases) {
            return false
        }

        if (normalized.length > 40) {
            return false
        }

        if (normalized.split(" ").size > 5) {
            return false
        }

        return true
    }

    private val unsafeIdentityAliases =
        setOf(
            "food",
            "beverage",
            "product",

            "fruit",
            "vegetable",
            "meat",
            "fish",
            "seafood",

            "cheese",
            "milk",
            "dairy",

            "honey",
            "syrup",

            "sausage",
            "ham",

            "nut",
            "nuts",
            "peanut",

            "oil",
            "sauce",
            "snack"
        )

    private fun String.isOutputAlias(): Boolean {

        val text =
            trim()

        if (text.isBlank()) {
            return false
        }

        if (text.length < 3) {
            return false
        }

        if (text.length > 80) {
            return false
        }

        var tokenCount =
            0

        var insideToken =
            false

        for (char in text) {

            val separator =
                char.isWhitespace() ||
                        char == '-' ||
                        char == '_' ||
                        char == ',' ||
                        char == ';' ||
                        char == ':' ||
                        char == '/'

            if (separator) {

                if (insideToken) {

                    tokenCount++

                    insideToken = false

                    if (tokenCount > 8) {
                        return false
                    }
                }

            } else {

                insideToken = true
            }
        }

        if (insideToken) {
            tokenCount++
        }

        return tokenCount in 1..8
    }

    private val matchOnlyAliases =
        setOf(
            "aromatic herb",
            "aromatic plant",
            "fresh vegetable",
            "leaf vegetable",
            "canned vegetable",
            "cereal grain",
            "cereals and potatoe",
            "fat and sauce",
            "pantry essentials",
            "dairy dessert",
            "fermented dairy dessert",
            "fermented milk product",
            "aliments d origine vegetale",
            "aliments a base de fruits et de legume",
            "aliments a base de plantes frai",
            "aliments et boissons a base de vegetaux"
        )

    private companion object {
        const val MAX_MATCH_KEY_FANOUT = 25
    }
}