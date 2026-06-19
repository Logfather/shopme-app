package de.shopme.presentation.developer.report

class SectionBuilder {

    private val entries =

        mutableListOf<BuildReportEntry>()

    fun entry(

        name: String,

        count: Int

    ) {

        entries +=

            BuildReportEntry(

                name = name,

                count = count

            )

    }

    fun build(): List<BuildReportEntry> =

        entries.toList()

}