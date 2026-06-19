package de.shopme.tools.report

data class FoodKnowledgeCoverageReport(

    val generatedAt: String,

    val catalogEntries: Int,

    val entries: List<FoodKnowledgeCoverageEntry>

) {

    val overallCoverage: Double

        get() {

            if (entries.isEmpty() || catalogEntries == 0) {

                return 0.0

            }

            val covered =

                entries.sumOf {

                    it.covered

                }

            val possible =

                catalogEntries * entries.size

            return covered.toDouble() * 100.0 /
                    possible.toDouble()

        }

}