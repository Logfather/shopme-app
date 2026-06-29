package de.shopme.tools.knowledge.off

import com.google.gson.GsonBuilder
import java.io.File

class OFFHivraExtractWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        extracts: List<OFFHivraExtract>,

        output: File

    ) {

        output.parentFile?.mkdirs()

        output.writeText(

            extracts.joinToString(
                separator = "\n"
            ) { extract ->

                gson.toJson(
                    extract
                )
            }
        )
    }
}