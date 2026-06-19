package de.shopme.tools.knowledge.compiler.shared.report

data class KnowledgeBuildReport(

    val artifacts: Int = 0,

    val entries: Int = 0,

    val warnings: List<ValidationMessage> = emptyList(),

    val errors: List<ValidationMessage> = emptyList()

) {

    val isSuccessful: Boolean
        get() = errors.isEmpty()

}