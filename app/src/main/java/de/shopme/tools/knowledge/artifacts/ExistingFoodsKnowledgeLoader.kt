package de.shopme.tools.knowledge.artifacts

import java.io.File

class ExistingFoodsKnowledgeLoader {

    fun load(
        input: File
    ): List<ExistingFoodKnowledgeEntry> {

        val entries =
            mutableListOf<ExistingFoodKnowledgeEntry>()

        var currentId: String? = null
        var insideKnowledge = false
        var knowledgeDepth = 0
        var currentDimensions = mutableSetOf<String>()

        input
            .readLines()
            .forEach { line ->

                if (currentId == null) {
                    currentId = extractStringValue(
                        line = line,
                        key = "id"
                    )
                }

                if (line.contains("\"knowledge\"")) {
                    insideKnowledge = true
                    knowledgeDepth = 0
                    currentDimensions = mutableSetOf()
                }

                if (insideKnowledge && knowledgeDepth == 1) {
                    extractObjectKey(line)?.let { key ->
                        currentDimensions += key
                    }
                }

                if (insideKnowledge) {

                    knowledgeDepth += line.count { it == '{' }
                    knowledgeDepth -= line.count { it == '}' }

                    if (knowledgeDepth == 0) {

                        val id =
                            currentId

                        if (id != null) {
                            entries += ExistingFoodKnowledgeEntry(
                                normalizedName = id,
                                knowledgeDimensions = currentDimensions.toSet()
                            )
                        }

                        currentId = null
                        insideKnowledge = false
                        currentDimensions = mutableSetOf()
                    }
                }
            }

        return entries
    }

    private fun extractStringValue(
        line: String,
        key: String
    ): String? {

        val regex =
            """"$key"\s*:\s*"([^"]+)"""".toRegex()

        return regex
            .find(line)
            ?.groupValues
            ?.get(1)
    }

    private fun extractObjectKey(
        line: String
    ): String? {

        val regex =
            """"([^"]+)"\s*:\s*\{""".toRegex()

        return regex
            .find(line)
            ?.groupValues
            ?.get(1)
    }
}