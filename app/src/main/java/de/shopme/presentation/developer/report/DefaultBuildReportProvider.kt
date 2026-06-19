package de.shopme.presentation.developer.report

class DefaultBuildReportProvider(

    private val factory: BuildReportFactory =

        DefaultBuildReportFactory()

) : BuildReportProvider {

    override fun report(): BuildReport =

        factory.create()

}