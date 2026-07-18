package de.shopme.testing.system.tools.knowledge.agribalyse

import de.shopme.tools.knowledge.agribalyse.parser.AgribalyseRawSourceReducer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgribalyseRawSourceReducerTest {

    @Test
    fun reduceAgribalyseRawSourceToSlimTsv() {

        val input =
            File(
                "../data/raw/agribalyse/" +
                        "AGRIBALYSE3.2_Tableur produits alimentaires_PublieAOUT25.xlsx"
            )

        val output =
            File(
                "../data/generated/agribalyse/" +
                        "agribalyse-foods.slim.tsv"
            )

        AgribalyseRawSourceReducer()
            .reduce(
                input = input,
                output = output,
                sheetName = "Synthese"
            )

        println("exists=${output.exists()}")
        println("length=${output.length()}")

        assertTrue(output.exists())
        assertTrue(output.length() > 0)

        val lines =
            output.readLines()

        println("lineCount=${lines.size}")
        println("firstLines:")
        lines.take(10).forEachIndexed { index, line ->
            println("[$index] $line")
        }

        assertTrue(lines.isNotEmpty())

        val header =
            lines.first()

        val columns =
            header.split("\t")

        println("columns=${columns.size}")
        println("header=$header")

        assertEquals(14, columns.size)

        assertEquals("code_agb", columns[0])
        assertEquals("code_ciqual", columns[1])
        assertEquals("food_group", columns[2])
        assertEquals("food_sub_group", columns[3])
        assertEquals("name_fr", columns[4])
        assertEquals("name_en", columns[5])
        assertEquals("data_quality_score", columns[6])
        assertEquals("environment_score_mpt_per_kg", columns[7])
        assertEquals("climate_total_kg_co2_eq_per_kg", columns[8])
        assertEquals("land_use_pt_per_kg", columns[9])
        assertEquals("water_deprivation_m3_per_kg", columns[10])
        assertEquals("climate_biogenic_kg_co2_eq_per_kg", columns[11])
        assertEquals("climate_fossil_kg_co2_eq_per_kg", columns[12])
        assertEquals("climate_land_use_change_kg_co2_eq_per_kg", columns[13])
    }
}