package de.shopme.presentation.developer.report

data class BuildReport(

    val summary: BuildReportSummary,

    val sections: List<BuildReportSection>

)