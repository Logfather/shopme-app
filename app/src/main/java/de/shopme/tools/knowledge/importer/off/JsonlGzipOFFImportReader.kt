package de.shopme.tools.knowledge.importer.off

import com.google.gson.Gson
import java.io.File
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

class JsonlGzipOFFImportReader(
    private val gson: Gson = Gson()
) : OFFImportReader {

    override fun read(
        file: File
    ): Sequence<OFFProduct> {

        return sequence {

            GZIPInputStream(
                file.inputStream()
            ).use { gzip ->

                InputStreamReader(gzip).buffered().useLines { lines ->

                    lines
                        .filter { it.isNotBlank() }
                        .forEach { line ->

                            yield(
                                gson.fromJson(
                                    line,
                                    OFFProduct::class.java
                                )
                            )
                        }
                }
            }
        }
    }
}