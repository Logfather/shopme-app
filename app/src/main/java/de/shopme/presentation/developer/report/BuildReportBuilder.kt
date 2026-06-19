package de.shopme.presentation.developer.report

class BuildReportBuilder {

    private val sections =

        mutableListOf<BuildReportSection>()

    fun section(

        title: String,

        build: SectionBuilder.() -> Unit

    ): BuildReportBuilder {

        val builder =

            SectionBuilder()

        builder.build()

        sections +=

            BuildReportSection(

                title = title,

                entries = builder.build()

            )

        return this

    }

    fun build(): BuildReport {

        return BuildReport(

            sections = sections.toList()

        )

    }

}