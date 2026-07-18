package de.shopme.tools.knowledge.ai.builder

fun RawKnowledgeInput.string(
    name: String
): String? =
    fields[name]
        ?.toString()
        ?.trim()
        ?.takeIf { it.isNotBlank() }

fun RawKnowledgeInput.double(
    name: String
): Double? =
    when (val value = fields[name]) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }

fun RawKnowledgeInput.int(
    name: String
): Int? =
    when (val value = fields[name]) {
        is Number -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> null
    }

fun RawKnowledgeInput.stringList(
    name: String
): List<String> =
    when (val value = fields[name]) {
        is Iterable<*> ->
            value
                .mapNotNull { it?.toString()?.trim() }
                .filter { it.isNotBlank() }

        is String ->
            value
                .split(",", ";", "|")
                .map { it.trim() }
                .filter { it.isNotBlank() }

        else ->
            emptyList()
    }

fun RawKnowledgeInput.hasField(
    name: String
): Boolean =
    fields[name] != null