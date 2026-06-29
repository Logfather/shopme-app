package de.shopme.tools.knowledge.agribalyse.loader

import java.io.File

class AgribalyseSyntheseCsvLoader {

    fun load(
        file: File
    ): List<Map<String, String>> {

        if (!file.exists()) {
            return emptyList()
        }

        val lines =
            file.readLines()

        if (lines.isEmpty()) {
            return emptyList()
        }

        val headers =
            parseCsvLine(
                lines.first()
            ).map {
                it.trim()
            }

        return lines
            .drop(1)
            .filter { it.isNotBlank() }
            .map { line ->

                val values =
                    parseCsvLine(line)

                headers.mapIndexed { index, header ->

                    header to
                            values.getOrElse(index) {
                                ""
                            }.trim()

                }.toMap()
            }
    }

    private fun parseCsvLine(
        line: String
    ): List<String> {

        val values =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var inQuotes =
            false

        var index =
            0

        while (index < line.length) {

            val char =
                line[index]

            if (char == '"') {

                if (
                    inQuotes &&
                    index + 1 < line.length &&
                    line[index + 1] == '"'
                ) {

                    current.append('"')
                    index++

                } else {

                    inQuotes =
                        !inQuotes
                }

            } else if (char == ',' && !inQuotes) {

                values.add(
                    current.toString()
                )

                current.clear()

            } else {

                current.append(char)
            }

            index++
        }

        values.add(
            current.toString()
        )

        return values
    }
}