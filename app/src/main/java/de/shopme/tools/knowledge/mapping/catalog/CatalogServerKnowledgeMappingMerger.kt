package de.shopme.tools.knowledge.mapping.catalog

class CatalogServerKnowledgeMappingMerger {

    fun merge(
        existing:
        CatalogServerKnowledgeMappings,
        incoming:
        List<CatalogServerKnowledgeMappings>
    ): CatalogServerKnowledgeMappingMergeResult {

        val existingByCatalogKey =
            existing.mappings
                .associateBy {
                    it.catalogKey
                }
                .toMutableMap()

        val allIncomingMappings =
            incoming
                .flatMap {
                    it.mappings
                }
                .sortedWith(
                    INCOMING_ORDER
                )

        var addedMappingCount =
            0

        var unchangedMappingCount =
            0

        val conflicts =
            mutableListOf<CatalogServerKnowledgeMappingConflict>()

        allIncomingMappings
            .groupBy {
                it.catalogKey
            }
            .toSortedMap()
            .forEach { (catalogKey, incomingForCatalogKey) ->

                val existingMapping =
                    existingByCatalogKey[
                        catalogKey
                    ]

                if (existingMapping != null) {

                    incomingForCatalogKey
                        .forEach { incomingMapping ->

                            if (
                                incomingMapping.serverKey ==
                                existingMapping.serverKey
                            ) {
                                unchangedMappingCount++
                            } else {
                                conflicts +=
                                    CatalogServerKnowledgeMappingConflict(
                                        catalogKey =
                                            catalogKey,
                                        retainedServerKey =
                                            existingMapping.serverKey,
                                        conflictingServerKey =
                                            incomingMapping.serverKey,
                                        retainedSourceArtifact =
                                            existingMapping.sourceArtifact,
                                        conflictingSourceArtifact =
                                            incomingMapping.sourceArtifact,
                                        reason =
                                            "Existing mapping is retained; " +
                                                    "incoming mapping selects a " +
                                                    "different server key"
                                    )
                            }
                        }

                    return@forEach
                }

                val incomingByServerKey =
                    incomingForCatalogKey
                        .groupBy {
                            it.serverKey
                        }
                        .toSortedMap()

                if (incomingByServerKey.size == 1) {

                    val selectedMapping =
                        selectRepresentative(
                            mappings =
                                incomingForCatalogKey
                        )

                    existingByCatalogKey[
                        catalogKey
                    ] = selectedMapping

                    addedMappingCount++

                    unchangedMappingCount +=
                        incomingForCatalogKey.size - 1

                    return@forEach
                }

                /*
                 * Mehrere neue Server-Keys für denselben Catalog-Key.
                 *
                 * Kein Mapping wird automatisch ausgewählt.
                 * Damit ist das Ergebnis unabhängig von der Reihenfolge
                 * der Eingabedateien.
                 */
                val orderedMappings =
                    incomingForCatalogKey
                        .sortedWith(
                            INCOMING_ORDER
                        )

                val diagnosticReference =
                    orderedMappings.first()

                orderedMappings
                    .drop(1)
                    .forEach { conflictingMapping ->

                        conflicts +=
                            CatalogServerKnowledgeMappingConflict(
                                catalogKey =
                                    catalogKey,
                                retainedServerKey =
                                    null,
                                conflictingServerKey =
                                    conflictingMapping.serverKey,
                                retainedSourceArtifact =
                                    null,
                                conflictingSourceArtifact =
                                    conflictingMapping.sourceArtifact,
                                reason =
                                    "Conflicting incoming mappings exist " +
                                            "without an established mapping; " +
                                            "candidate server keys include " +
                                            "'${diagnosticReference.serverKey}' " +
                                            "and " +
                                            "'${conflictingMapping.serverKey}'"
                            )
                    }
            }

        val mergedMappings =
            existingByCatalogKey
                .values
                .sortedWith(
                    CatalogServerKnowledgeMappings.MAPPING_ORDER
                )

        val sortedConflicts =
            conflicts.sortedWith(
                CatalogServerKnowledgeMappingConflict.ORDER
            )

        return CatalogServerKnowledgeMappingMergeResult(
            mappings =
                CatalogServerKnowledgeMappings(
                    version =
                        CatalogServerKnowledgeMappings.CURRENT_VERSION,
                    mappings =
                        mergedMappings
                ),
            report =
                CatalogServerKnowledgeMappingMergeReport(
                    existingMappingCount =
                        existing.mappings.size,
                    incomingMappingCount =
                        allIncomingMappings.size,
                    addedMappingCount =
                        addedMappingCount,
                    unchangedMappingCount =
                        unchangedMappingCount,
                    conflictCount =
                        sortedConflicts.size,
                    totalMappingCount =
                        mergedMappings.size,
                    conflicts =
                        sortedConflicts
                )
        )
    }


    private fun selectRepresentative(
        mappings: List<CatalogServerKnowledgeMapping>
    ): CatalogServerKnowledgeMapping {

        require(mappings.isNotEmpty()) {
            "mappings must not be empty"
        }

        /*
         * Bei identischem Catalog-Key und Server-Key:
         *
         * 1. EXACT vor AI_VALIDATED
         * 2. höhere Confidence
         * 3. sourceArtifact alphabetisch
         * 4. reason alphabetisch
         */
        return mappings
            .sortedWith(
                REPRESENTATIVE_ORDER
            )
            .first()
    }


    companion object {

        private val INCOMING_ORDER:
                Comparator<CatalogServerKnowledgeMapping> =
            compareBy<CatalogServerKnowledgeMapping> {
                it.catalogKey
            }.thenBy {
                it.serverKey
            }.thenBy {
                it.sourceArtifact
            }.thenByDescending {
                it.confidence
            }


        private val REPRESENTATIVE_ORDER:
                Comparator<CatalogServerKnowledgeMapping> =
            compareBy<CatalogServerKnowledgeMapping> {
                when (it.method) {
                    CatalogServerKnowledgeMappingMethod.EXACT ->
                        0

                    CatalogServerKnowledgeMappingMethod.AI_VALIDATED ->
                        1
                }
            }.thenByDescending {
                it.confidence
            }.thenBy {
                it.sourceArtifact
            }.thenBy {
                it.reason
            }
    }
}