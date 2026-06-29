package de.shopme.tools.knowledge.compiler.candidate

data class CanonicalKnowledgeCandidateValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)