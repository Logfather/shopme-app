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

        val builtSections =

            sections.toList()

        val artifacts =

            builtSections.sumOf {

                it.entries.size

            }

        val totalEntries =

            builtSections.sumOf { section ->

                section.entries.sumOf {

                    it.count

                }

            }

        return BuildReport(

            summary = BuildReportSummary(

                sections = builtSections.size,

                artifacts = artifacts,

                totalEntries = totalEntries

            ),

            sections = builtSections

        )

    }

}